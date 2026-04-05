# Проект "Облачное хранилище файлов"
## Описание
Многопользовательское файловое облако. Пользователи сервиса могут использовать его для загрузки и хранения файлов. Проект написан в стиле REST API.

ТЗ проекта – [здесь](https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/)

## Использованные технологии / инструменты

- Spring Boot
- Spring Security
- Spring Sessions
- PostgreSQL
- Liquibase
- Redis
- Minio
- Mapstruct
- Gradle
- Swagger
- JUnit 5
- Testcontainers
- Docker

## Инструкция по запуску
1. Клонируйте репозиторий
2. Установите Docker и запустите его
3. Создайте в корне проекта `.env` файл и заполните по следующему шаблону:
   
```
# Необходимо:

POSTGRES_DB=testdatabase
POSTGRES_USER=testuser
POSTGRES_PASSWORD=testpassword

MINIO_URL=http://minio:9000
MINIO_ROOT_USER=minio_dev_user
MINIO_ROOT_PASSWORD=minio_dev_secret

# Опционально

SESSION_TIMEOUT=

SPRING_MULTIPART_MAX_FILE_SIZE=
SPRING_MULTIPART_MAX_REQUEST_SIZE=

MAX_USER_STORAGE=
MAX_SERVER_STORAGE=

```
4. В папке репозитория выполните:

```bash
docker compose up -d --build
```
5. Теперь проект будет доступен по адресу `http://localhost`
