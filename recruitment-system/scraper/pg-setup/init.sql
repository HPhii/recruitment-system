-- Create function to update last_modified timestamp
CREATE OR REPLACE FUNCTION update_last_modified()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_modified = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create companies table
CREATE TABLE IF NOT EXISTS companies (
    company_id BIGINT PRIMARY KEY,
    company_name TEXT NOT NULL,
    company_logo TEXT,
    company_size TEXT,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for companies
CREATE OR REPLACE TRIGGER update_companies
BEFORE UPDATE ON companies
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create locations table
CREATE TABLE IF NOT EXISTS locations (
    location_id BIGINT PRIMARY KEY,
    address TEXT,
    city_id INTEGER,
    district_id INTEGER,
    geo_loc_lat DECIMAL,
    geo_loc_lon DECIMAL,
    city_name TEXT,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for locations
CREATE OR REPLACE TRIGGER update_locations
BEFORE UPDATE ON locations
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create jobs table
CREATE TABLE IF NOT EXISTS jobs (
    job_id BIGINT PRIMARY KEY,
    job_title TEXT NOT NULL,
    job_url TEXT,
    company_id BIGINT REFERENCES companies(company_id),
    approved_on TIMESTAMP,
    expired_on TIMESTAMP,
    online_on TIMESTAMP,
    salary_min DECIMAL,
    salary_max DECIMAL,
    pretty_salary TEXT,
    job_description TEXT,
    job_requirement TEXT,
    years_of_experience INTEGER,
    job_level TEXT,
    language_selected TEXT,
    num_of_applications INTEGER,
    is_salary_visible BOOLEAN,
    is_show_logo BOOLEAN,
    is_show_logo_in_search BOOLEAN,
    visibility_display BOOLEAN,
    priority_order TIMESTAMP,
    is_mobile_hot_job BOOLEAN,
    is_mobile_top_job BOOLEAN,
    is_bold_and_red_job BOOLEAN,
    is_urgent_job BOOLEAN,
    is_top_priority BOOLEAN,
    job_functions_v3_id INTEGER,
    group_job_functions_v3_id INTEGER,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for jobs
CREATE OR REPLACE TRIGGER update_jobs
BEFORE UPDATE ON jobs
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create job_locations table (junction table for jobs and locations)
CREATE TABLE IF NOT EXISTS job_locations (
    job_id BIGINT REFERENCES jobs(job_id),
    location_id BIGINT REFERENCES locations(location_id),
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW(),
    PRIMARY KEY (job_id, location_id)
);

-- Create trigger for job_locations
CREATE OR REPLACE TRIGGER update_job_locations
BEFORE UPDATE ON job_locations
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create skills table
CREATE TABLE IF NOT EXISTS skills (
    skill_id BIGINT PRIMARY KEY,
    skill_name TEXT NOT NULL,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for skills
CREATE OR REPLACE TRIGGER update_skills
BEFORE UPDATE ON skills
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create job_skills table (junction table for jobs and skills)
CREATE TABLE IF NOT EXISTS job_skills (
    job_id BIGINT REFERENCES jobs(job_id),
    skill_id BIGINT REFERENCES skills(skill_id),
    skill_weight INTEGER,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW(),
    PRIMARY KEY (job_id, skill_id)
);

-- Create trigger for job_skills
CREATE OR REPLACE TRIGGER update_job_skills
BEFORE UPDATE ON job_skills
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create benefits table
CREATE TABLE IF NOT EXISTS benefits (
    benefit_id BIGINT PRIMARY KEY,
    benefit_name TEXT NOT NULL,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for benefits
CREATE OR REPLACE TRIGGER update_benefits
BEFORE UPDATE ON benefits
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create job_benefits table (junction table for jobs and benefits)
CREATE TABLE IF NOT EXISTS job_benefits (
    job_id BIGINT REFERENCES jobs(job_id),
    benefit_id BIGINT REFERENCES benefits(benefit_id),
    benefit_value TEXT,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW(),
    PRIMARY KEY (job_id, benefit_id)
);

-- Create trigger for job_benefits
CREATE OR REPLACE TRIGGER update_job_benefits
BEFORE UPDATE ON job_benefits
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create industries_v3 table
CREATE TABLE IF NOT EXISTS industries_v3 (
    industry_v3_id INTEGER PRIMARY KEY,
    industry_v3_name TEXT NOT NULL,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for industries_v3
CREATE OR REPLACE TRIGGER update_industries_v3
BEFORE UPDATE ON industries_v3
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create job_industries_v3 table (junction table for jobs and industries_v3)
CREATE TABLE IF NOT EXISTS job_industries_v3 (
    job_id BIGINT REFERENCES jobs(job_id),
    industry_v3_id INTEGER REFERENCES industries_v3(industry_v3_id),
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW(),
    PRIMARY KEY (job_id, industry_v3_id)
);

-- Create trigger for job_industries_v3
CREATE OR REPLACE TRIGGER update_job_industries_v3
BEFORE UPDATE ON job_industries_v3
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create job_functions_v3 table
CREATE TABLE IF NOT EXISTS job_functions_v3 (
    job_function_v3_id INTEGER PRIMARY KEY,
    job_function_v3_name TEXT NOT NULL,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for job_functions_v3
CREATE OR REPLACE TRIGGER update_job_functions_v3
BEFORE UPDATE ON job_functions_v3
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();

-- Create group_job_functions_v3 table
CREATE TABLE IF NOT EXISTS group_job_functions_v3 (
    group_job_function_v3_id INTEGER PRIMARY KEY,
    group_job_function_v3_name TEXT NOT NULL,
    created_at TIMESTAMP(0) DEFAULT NOW(),
    last_modified TIMESTAMP(0) DEFAULT NOW()
);

-- Create trigger for group_job_functions_v3
CREATE OR REPLACE TRIGGER update_group_job_functions_v3
BEFORE UPDATE ON group_job_functions_v3
FOR EACH ROW
EXECUTE FUNCTION update_last_modified();