import psycopg2

def get_db_connection():
    conn = psycopg2.connect(
        dbname="recruitment",
        user="postgres",
        password="postgres",
        host="db",
        port="5432"
    )
    return conn

def get_cursor(conn):
    return conn.cursor()

def close_db(conn, cur):
    cur.close()
    conn.close()