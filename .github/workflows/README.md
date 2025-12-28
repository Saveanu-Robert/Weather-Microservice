# GitHub Actions Workflows

## Dependency Management

### Daily Dependency Check (Simple)
**File:** `dependency-check-simple.yml`

Runs every day at 9:00 AM UTC to check for dependency updates.

**What it does:**
- Checks all Maven dependencies and plugins for updates
- Fails the workflow if updates are available (red X in Actions tab)
- Uploads a detailed log file with all available updates

**How to use:**
- Check the Actions tab daily for red X markers
- Download the artifact to see what needs updating
- Manually trigger: Go to Actions → Daily Dependency Check (Simple) → Run workflow

### Daily Dependency Check (Full)
**File:** `dependency-updates.yml`

More comprehensive workflow that creates GitHub issues when updates are found.

**What it does:**
- Checks for dependency and plugin updates
- Generates a formatted report
- Creates a GitHub issue with update details (requires repository permissions)
- Updates existing issue if already open

**Setup:**
- Ensure repository has "Read and write permissions" for workflows
  - Go to Settings → Actions → General → Workflow permissions
  - Select "Read and write permissions"

## Manual Dependency Updates

To update dependencies manually:

```bash
# Check for updates
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates

# Update a specific dependency
mvn versions:use-latest-versions -Dincludes=groupId:artifactId

# Update all dependencies (use with caution)
mvn versions:use-latest-releases
```

## Version Properties

All versions are managed in `pom.xml` properties section for easy updates:
- `spring-boot.version` - Spring Boot framework
- `lombok.version` - Lombok
- `caffeine.version` - Caffeine cache
- `resilience4j.version` - Resilience4j
- ... and more

Update any version by changing the property value in `pom.xml`.
