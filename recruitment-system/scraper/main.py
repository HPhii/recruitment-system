import asyncio
import logging
import json
import time
from datetime import datetime
from typing import List, Dict, Optional
import aiohttp
import psycopg2
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

# logging config
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('scraper.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# json file config
with open('config.json', 'r') as f:
    CONFIG = json.load(f)

# db conn
def get_db_connection():
    return psycopg2.connect(
        dbname=CONFIG['database']['dbname'],
        user=CONFIG['database']['user'],
        password=CONFIG['database']['password'],
        host=CONFIG['database']['host'],
        port=CONFIG['database']['port']
    )

def get_cursor(conn):
    return conn.cursor()

def close_db(conn, cur):
    cur.close()
    conn.close()

# headers and payload
HEADERS = CONFIG['request_headers']
PAYLOAD_TEMPLATE = {
    "userId": 0,
    "query": "",
    "filter": [],
    "ranges": [],
    "order": [],
    "hitsPerPage": 50,
    "page": 0,
    "retrieveFields": CONFIG['retrieve_fields'],
    "summaryVersion": ""
}

# Asynchronous function to fetch jobs from the API
@retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=4, max=10), 
       retry=retry_if_exception_type(aiohttp.ClientError))
async def fetch_jobs(session: aiohttp.ClientSession, page: int) -> List[Dict]:
    payload = PAYLOAD_TEMPLATE.copy()
    payload['page'] = page
    async with session.post(CONFIG['api_endpoint'], headers=HEADERS, json=payload) as response:
        if response.status == 200:
            data = await response.json()
            return data['data']
        else:
            logger.error(f"Error fetching page {page}: {response.status}")
            return []

# Crawl jobs asynchronously
async def crawl_jobs(max_pages: int) -> List[Dict]:
    all_jobs = []
    async with aiohttp.ClientSession() as session:
        tasks = [fetch_jobs(session, page) for page in range(max_pages)]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        for page, result in enumerate(results):
            if isinstance(result, Exception):
                logger.error(f"Failed to fetch page {page}: {str(result)}")
            else:
                all_jobs.extend(result)
        logger.info(f"Crawled {len(all_jobs)} jobs from {max_pages} pages")
    return all_jobs

# incremental crawling
def job_exists(cur, job_id: str) -> bool:
    cur.execute("SELECT EXISTS(SELECT 1 FROM jobs WHERE job_id = %s)", (job_id,))
    return cur.fetchone()[0]

# Bulk insert jobs into the database
def bulk_insert_jobs(conn, cur, jobs: List[Dict]):
    new_jobs = [job for job in jobs if not job_exists(cur, job['jobId'])]
    if not new_jobs:
        logger.info("No new jobs to insert")
        return

    company_values = [(j['companyId'], j['companyName'], j['companyLogo'], j.get('companySize', '')) 
                     for j in new_jobs]
    cur.executemany("""
        INSERT INTO companies (company_id, company_name, company_logo, company_size)
        VALUES (%s, %s, %s, %s)
        ON CONFLICT (company_id) DO UPDATE SET
            company_name = EXCLUDED.company_name,
            company_logo = EXCLUDED.company_logo,
            company_size = EXCLUDED.company_size
    """, company_values)
    
    for job in new_jobs:
        job_function_id = insert_job_functions_v3(cur, job.get('jobFunctionsV3', {}))
        group_job_function_id = insert_group_job_functions_v3(cur, job.get('groupJobFunctionsV3', {}))
        insert_job(cur, job, job_function_id, group_job_function_id)
        insert_locations_and_job_locations(cur, job)
        insert_skills(cur, job)
        insert_benefits(cur, job)
        insert_industries_v3(cur, job)

    conn.commit()
    logger.info(f"Inserted {len(new_jobs)} new jobs")


def insert_job_functions_v3(cur, job_functions_v3) -> Optional[str]:
    if job_functions_v3:
        cur.execute("""
            INSERT INTO job_functions_v3 (job_function_v3_id, job_function_v3_name)
            VALUES (%s, %s)
            ON CONFLICT (job_function_v3_id) DO NOTHING
        """, (job_functions_v3['jobFunctionV3Id'], job_functions_v3['jobFunctionV3Name']))
        return job_functions_v3['jobFunctionV3Id']
    return None

def insert_group_job_functions_v3(cur, group_job_functions_v3) -> Optional[str]:
    if group_job_functions_v3:
        cur.execute("""
            INSERT INTO group_job_functions_v3 (group_job_function_v3_id, group_job_function_v3_name)
            VALUES (%s, %s)
            ON CONFLICT (group_job_function_v3_id) DO NOTHING
        """, (group_job_functions_v3['groupJobFunctionV3Id'], group_job_functions_v3['groupJobFunctionV3Name']))
        return group_job_functions_v3['groupJobFunctionV3Id']
    return None

def insert_job(cur, job, job_function_id, group_job_function_id):
    cur.execute("""
        INSERT INTO jobs (
            job_id, job_title, job_url, company_id, approved_on, expired_on, online_on,
            salary_min, salary_max, pretty_salary, job_description, job_requirement,
            years_of_experience, job_level, language_selected, num_of_applications,
            is_salary_visible, is_show_logo, is_show_logo_in_search, visibility_display,
            priority_order, is_mobile_hot_job, is_mobile_top_job, is_bold_and_red_job,
            is_urgent_job, is_top_priority, job_functions_v3_id, group_job_functions_v3_id
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (job_id) DO UPDATE SET
            job_title = EXCLUDED.job_title,
            job_url = EXCLUDED.job_url,
            company_id = EXCLUDED.company_id,
            approved_on = EXCLUDED.approved_on,
            expired_on = EXCLUDED.expired_on,
            online_on = EXCLUDED.online_on,
            salary_min = EXCLUDED.salary_min,
            salary_max = EXCLUDED.salary_max,
            pretty_salary = EXCLUDED.pretty_salary,
            job_description = EXCLUDED.job_description,
            job_requirement = EXCLUDED.job_requirement,
            years_of_experience = EXCLUDED.years_of_experience,
            job_level = EXCLUDED.job_level,
            language_selected = EXCLUDED.language_selected,
            num_of_applications = EXCLUDED.num_of_applications,
            is_salary_visible = EXCLUDED.is_salary_visible,
            is_show_logo = EXCLUDED.is_show_logo,
            is_show_logo_in_search = EXCLUDED.is_show_logo_in_search,
            visibility_display = EXCLUDED.visibility_display,
            priority_order = EXCLUDED.priority_order,
            is_mobile_hot_job = EXCLUDED.is_mobile_hot_job,
            is_mobile_top_job = EXCLUDED.is_mobile_top_job,
            is_bold_and_red_job = EXCLUDED.is_bold_and_red_job,
            is_urgent_job = EXCLUDED.is_urgent_job,
            is_top_priority = EXCLUDED.is_top_priority,
            job_functions_v3_id = EXCLUDED.job_functions_v3_id,
            group_job_functions_v3_id = EXCLUDED.group_job_functions_v3_id,
            last_modified = NOW()
    """, (
        job['jobId'], job['jobTitle'], job['jobUrl'], job['companyId'], job['approvedOn'],
        job['expiredOn'], job['onlineOn'], job.get('salaryMin'), job.get('salaryMax'),
        job.get('prettySalary'), job['jobDescription'], job['jobRequirement'],
        job.get('yearsOfExperience'), job.get('jobLevel'), job.get('languageSelected'),
        job.get('numOfApplications'), job.get('isSalaryVisible'), job.get('isShowLogo'),
        job.get('isShowLogoInSearch'), job.get('visibilityDisplay'), job.get('priorityOrder'),
        job.get('isMobileHotJob'), job.get('isMobileTopJob'), job.get('isBoldAndRedJob'),
        job.get('isUrgentJob'), job.get('isTopPriority'),
        job_function_id, group_job_function_id
    ))

def insert_locations_and_job_locations(cur, job):
    locations = job.get('workingLocations', [])
    for location in locations:
        geo_loc = location.get('geoLoc', {})
        cur.execute("""
            INSERT INTO locations (location_id, address, city_id, district_id, geo_loc_lat, geo_loc_lon, city_name)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (location_id) DO NOTHING
        """, (
            location['workingLocationId'], location['address'], location.get('cityId'),
            location.get('districtId'), geo_loc.get('lat'), geo_loc.get('lon'), location.get('cityName')
        ))
        cur.execute("""
            INSERT INTO job_locations (job_id, location_id)
            VALUES (%s, %s)
            ON CONFLICT DO NOTHING
        """, (job['jobId'], location['workingLocationId']))

def insert_skills(cur, job):
    skills = job.get('skills', [])
    for skill in skills:
        cur.execute("""
            INSERT INTO skills (skill_id, skill_name)
            VALUES (%s, %s)
            ON CONFLICT (skill_id) DO NOTHING
        """, (skill['skillId'], skill['skillName']))
        cur.execute("""
            INSERT INTO job_skills (job_id, skill_id, skill_weight)
            VALUES (%s, %s, %s)
            ON CONFLICT (job_id, skill_id) DO UPDATE SET skill_weight = EXCLUDED.skill_weight
        """, (job['jobId'], skill['skillId'], skill['skillWeight']))

def insert_benefits(cur, job):
    benefits = job.get('benefits', [])
    for benefit in benefits:
        cur.execute("""
            INSERT INTO benefits (benefit_id, benefit_name)
            VALUES (%s, %s)
            ON CONFLICT (benefit_id) DO NOTHING
        """, (benefit['benefitId'], benefit['benefitName']))
        cur.execute("""
            INSERT INTO job_benefits (job_id, benefit_id, benefit_value)
            VALUES (%s, %s, %s)
            ON CONFLICT (job_id, benefit_id) DO UPDATE SET benefit_value = EXCLUDED.benefit_value
        """, (job['jobId'], benefit['benefitId'], benefit['benefitValue']))

def insert_industries_v3(cur, job):
    cur.execute("DELETE FROM job_industries_v3 WHERE job_id = %s", (job['jobId'],))
    industries = job.get('industriesV3', [])
    for industry in industries:
        cur.execute("""
            INSERT INTO industries_v3 (industry_v3_id, industry_v3_name)
            VALUES (%s, %s)
            ON CONFLICT (industry_v3_id) DO NOTHING
        """, (industry['industryV3Id'], industry['industryV3Name']))
        cur.execute("""
            INSERT INTO job_industries_v3 (job_id, industry_v3_id)
            VALUES (%s, %s)
        """, (job['jobId'], industry['industryV3Id']))


async def main():
    try:
        conn = get_db_connection()
        cur = get_cursor(conn)
        
        max_pages = CONFIG['scraper']['max_pages']
        all_jobs = await crawl_jobs(max_pages)
        
        if all_jobs:
            bulk_insert_jobs(conn, cur, all_jobs)
        else:
            logger.warning("No jobs retrieved")
        
        close_db(conn, cur)
    except Exception as e:
        logger.error(f"Main process failed: {str(e)}")
        raise

# Schedule the scraper
if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    # Run the main function immediately
    loop.run_until_complete(main())
    # Set up the scheduler for subsequent runs
    scheduler = AsyncIOScheduler(event_loop=loop)
    scheduler.add_job(main, 'interval', minutes=CONFIG['scraper']['interval_minutes'])
    scheduler.start()
    try:
        loop.run_forever()
    except (KeyboardInterrupt, SystemExit):
        scheduler.shutdown()
        logger.info("Scheduler stopped")