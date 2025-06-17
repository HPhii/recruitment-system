import requests
import time

def fetch_jobs(page, headers, payload_template):
    payload = payload_template.copy()
    payload['page'] = page
    response = requests.post("https://ms.vietnamworks.com/job-search/v1.0/search", headers=headers, json=payload)
    if response.status_code == 200:
        return response.json()['data']
    else:
        print(f"Error fetching page {page}: {response.status_code}")
        return []

def crawl_jobs(max_pages, headers, payload_template):
    all_jobs = []
    for page in range(max_pages):
        print(f"Đang lấy trang {page + 1}/{max_pages}")
        jobs = fetch_jobs(page, headers, payload_template)
        all_jobs.extend(jobs)
        time.sleep(2)
    return all_jobs