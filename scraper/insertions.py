# insertions.py
def insert_company(cur, company):
    cur.execute("""
        INSERT INTO companies (company_id, company_name, company_logo, company_size)
        VALUES (%s, %s, %s, %s)
        ON CONFLICT (company_id) DO UPDATE SET
            company_name = EXCLUDED.company_name,
            company_logo = EXCLUDED.company_logo,
            company_size = EXCLUDED.company_size
    """, (company['companyId'], company['companyName'], company['companyLogo'], company.get('companySize', '')))

def insert_job_functions_v3(cur, job_functions_v3):
    if job_functions_v3:
        cur.execute("""
            INSERT INTO job_functions_v3 (job_function_v3_id, job_function_v3_name)
            VALUES (%s, %s)
            ON CONFLICT (job_function_v3_id) DO NOTHING
        """, (job_functions_v3['jobFunctionV3Id'], job_functions_v3['jobFunctionV3Name']))
        return job_functions_v3['jobFunctionV3Id']
    return None

def insert_group_job_functions_v3(cur, group_job_functions_v3):
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
    for location in job['workingLocations']:
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
    for skill in job['skills']:
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
    for benefit in job['benefits']:
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
    for industry in job['industriesV3']:
        cur.execute("""
            INSERT INTO industries_v3 (industry_v3_id, industry_v3_name)
            VALUES (%s, %s)
            ON CONFLICT (industry_v3_id) DO NOTHING
        """, (industry['industryV3Id'], industry['industryV3Name']))
        cur.execute("""
            INSERT INTO job_industries_v3 (job_id, industry_v3_id)
            VALUES (%s, %s)
        """, (job['jobId'], industry['industryV3Id']))