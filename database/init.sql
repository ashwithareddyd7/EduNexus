-- EduNexus database bootstrap. Run once as the postgres superuser.
-- Usage (PowerShell, from the project root):
--   psql -U postgres -h localhost -v app_password=YOUR_PASSWORD -f database/init.sql

CREATE ROLE edunexus_app WITH LOGIN PASSWORD :'app_password';

CREATE DATABASE edunexus
    OWNER edunexus_app
    ENCODING 'UTF8';