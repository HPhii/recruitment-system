# main.py
from db import get_db_connection, get_cursor, close_db
from api import crawl_jobs
from insertions import (
    insert_company, insert_job_functions_v3, insert_group_job_functions_v3,
    insert_job, insert_locations_and_job_locations, insert_skills,
    insert_benefits, insert_industries_v3
)

# API headers and payload template
headers = {
    "Accept": "*/*",
    "Accept-Encoding": "gzip, deflate, br, zstd",
    "Accept-Language": "vi",
    "Connection": "keep-alive",
    "Content-Type": "application/json",
    "Host": "ms.vietnamworks.com",
    "Origin": "https://www.vietnamworks.com",
    "Referer": "https://www.vietnamworks.com/",
    "Sec-Fetch-Dest": "empty",
    "Sec-Fetch-Mode": "cors",
    "Sec-Fetch-Site": "same-site",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36",
    "X-Source": "Page-Container",
    "sec-ch-ua": '"Google Chrome";v="137", "Chromium";v="137", "Not/A)Brand";v="24"',
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": '"Windows"'
}

retrieveFields = [
    "jobId", "jobTitle", "jobUrl", "companyName", "companyId", "workingLocations",
    "approvedOn", "expiredOn", "onlineOn", "salaryMin", "salaryMax", "prettySalary",
    "benefits", "jobDescription", "jobRequirement", "skills", "yearsOfExperience",
    "companyLogo", "companySize", "industries", "industriesV3", "jobFunctionsV3",
    "groupJobFunctionsV3", "jobLevel", "languageSelected", "numOfApplications",
    "isSalaryVisible", "isShowLogo", "isShowLogoInSearch", "visibilityDisplay",
    "priorityOrder", "isMobileHotJob", "isMobileTopJob", "isBoldAndRedJob",
    "isUrgentJob", "isTopPriority"
]

payload_template = {
    "userId": 0,
    "query": "",
    "filter": [],
    "ranges": [],
    "order": [],
    "hitsPerPage": 50,
    "page": 0,
    "retrieveFields": retrieveFields,
    "summaryVersion": ""
}

def main():
    conn = get_db_connection()
    cur = get_cursor(conn)

    max_pages = 3
    all_jobs = crawl_jobs(max_pages, headers, payload_template)

    # Chèn dữ liệu vào database
    for job in all_jobs:
        insert_company(cur, job)
        job_function_id = insert_job_functions_v3(cur, job.get('jobFunctionsV3', {}))
        group_job_function_id = insert_group_job_functions_v3(cur, job.get('groupJobFunctionsV3', {}))
        insert_job(cur, job, job_function_id, group_job_function_id)
        insert_locations_and_job_locations(cur, job)
        insert_skills(cur, job)
        insert_benefits(cur, job)
        insert_industries_v3(cur, job)

    conn.commit()
    close_db(conn, cur)

    print(f"Đã lấy và lưu {len(all_jobs)} job vào database")

if __name__ == "__main__":
    main()