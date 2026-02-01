# Backend API

A Spring Boot REST API for managing photos, profile data, and static assets. Deployed on AWS EC2 with Docker, using DynamoDB for data storage and S3 for file uploads.

## Tech Stack

- **Java 21** + **Spring Boot 3**
- **AWS DynamoDB** - Data persistence
- **AWS S3** - File storage with presigned URLs
- **Docker** - Containerization
- **GitHub Actions** - CI/CD with semantic-release
- **Cloudflare** - DNS and SSL proxy

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Health check |
| GET | `/v1/images` | List photos (paginated) |
| PUT | `/v1/images` | Get presigned URL for photo upload |
| POST | `/v1/images` | Save photo metadata |
| GET | `/v1/selfie` | Get selfie URL |
| PUT | `/v1/selfie` | Get presigned URL for selfie upload |
| GET | `/v1/resume` | Get resume URL |
| PUT | `/v1/resume` | Get presigned URL for resume upload |
| GET | `/v1/social-links` | Get social links |
| POST | `/v1/social-links` | Save social links |
| GET | `/v1/site-message` | Get site message |
| POST | `/v1/site-message` | Save site message |

## Project Structure

```
src/main/java/com/api/
├── App.java                 # Main application entry point
├── config/                  # Configuration classes
│   ├── AwsConfig.java       # AWS clients (DynamoDB, S3)
│   ├── AwsProperties.java   # Externalized AWS configuration
│   ├── CorsConfig.java      # CORS settings (local profile)
│   └── GlobalExceptionHandler.java
├── controller/              # REST controllers
│   ├── HealthController.java
│   ├── PhotoController.java
│   └── ProfileController.java
├── service/                 # Business logic interfaces
│   ├── PhotoService.java
│   └── ProfileService.java
├── service/impl/            # Service implementations
│   ├── PhotoServiceImpl.java
│   └── ProfileServiceImpl.java
├── dto/                     # Data Transfer Objects
│   ├── request/
│   └── response/
├── common/                  # Shared utilities
│   ├── ApiResponse.java
│   └── Constant.java
└── util/
    └── DateTimeUtil.java
```

## Local Development

### Prerequisites

- Java 21
- Maven
- AWS credentials configured

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/backend.git
   cd backend
   ```

2. **Create secrets file:**
   ```bash
   cp src/main/resources/application-secrets.properties.example \
      src/main/resources/application-secrets.properties
   ```

3. **Add your AWS credentials to `application-secrets.properties`:**
   ```properties
   aws.access-key-id=YOUR_ACCESS_KEY
   aws.secret-access-key=YOUR_SECRET_KEY
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. **Test the API:**
   ```bash
   curl http://localhost:8080/
   curl http://localhost:8080/v1/images?page=10
   ```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region | `us-east-1` |
| `PHOTOS_BUCKET` | S3 bucket for photos | `photos-jin` |
| `ASSETS_BUCKET` | S3 bucket for assets | `generic-jin` |
| `PHOTOS_CLOUDFRONT` | CloudFront URL for photos | - |
| `ASSETS_CLOUDFRONT` | CloudFront URL for assets | - |
| `PROFILE_TABLE` | DynamoDB table for profile | `tbl_profile` |
| `PHOTO_TABLE` | DynamoDB table for photos | `tbl_photo` |
| `KEYSTORE_PATH` | SSL keystore path | - |
| `KEYSTORE_PASSWORD` | SSL keystore password | - |

### Profiles

- **local** - Disables SSL, enables CORS for localhost
- **default** - Production settings with SSL enabled

## AWS Resources

### DynamoDB Tables

- `tbl_photo` - Photo metadata (partition key: `imageID`)
- `tbl_profile` - Profile data (partition key: `profileId`)

### S3 Buckets

- `photos-jin` - Gallery photos
- `generic-jin` - Static assets (selfie, resume)

## Deployment

### CI/CD Pipeline

The project uses GitHub Actions with semantic-release for automated deployments:

1. **Push to `main`** triggers the Release workflow
2. **Semantic-release** analyzes commits and creates a new release if needed
3. **Release published** triggers the Deploy workflow
4. **Docker image** is built and pushed to Docker Hub
5. **EC2 deployment** pulls and runs the new image

### Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```bash
fix: description    # Patch release (v1.0.0 → v1.0.1)
feat: description   # Minor release (v1.0.0 → v1.1.0)
feat!: description  # Major release (v1.0.0 → v2.0.0)
```

### Manual Deployment

```bash
# Build Docker image
docker build -t backend:latest .

# Run container
docker run -d \
  -e KEYSTORE_PASSWORD='your-password' \
  -e KEYSTORE_PATH='classpath:origin.p12' \
  -p 443:8080 \
  --name backend \
  backend:latest
```

## Security

- **Never commit** `application-secrets.properties` (contains AWS credentials)
- Use **IAM Instance Profile** for EC2 deployments (no credentials needed)
- SSL/TLS enabled with Cloudflare Origin Certificate

## License

MIT License - see [LICENSE](LICENSE) for details.
