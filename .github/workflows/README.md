# GitHub Actions Workflows

This directory contains automated workflows for the Weather Microservice project.

## Active Workflows

### CI Pipeline (`ci.yml`)
Runs on every push and pull request to validate code quality and functionality.

**Triggers:**
- Push to any branch
- Pull requests

**Actions:**
- Builds the project with Maven
- Runs all tests (unit and integration)
- Generates code coverage reports (JaCoCo)
- Runs Checkstyle and Spotless code quality checks
- Uploads test and coverage reports as artifacts

### Dependency Updates (`dependency-updates.yml`)
Automatically checks for and updates Maven dependencies daily.

**Triggers:**
- Daily at 9:00 AM UTC (cron: `0 9 * * *`)
- Manual trigger via workflow_dispatch

**Actions:**
- Checks for dependency and plugin updates
- Updates dependencies to latest stable versions (excludes major version bumps)
- Verifies the build still works
- Creates a Pull Request with the changes if updates are available

**Configuration:**
- Uses `maven-version-rules.xml` to filter out alpha, beta, RC, and snapshot versions
- Only suggests stable release versions
- Labels PRs with `dependencies` and `automated`

**Manual Trigger:**
You can manually trigger this workflow from the GitHub Actions tab.

### Dependabot Auto-Merge (`dependabot-auto-merge.yml`)
Automatically merges Dependabot PRs for patch and minor updates after CI passes.

**Triggers:**
- Dependabot pull requests

**Actions:**
- Waits for CI pipeline to complete
- Auto-merges patch and minor version updates
- Requires manual review for major version updates

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

## Workflow Permissions

All workflows use minimal required permissions:
- `dependency-updates.yml`: Requires `contents: write` and `pull-requests: write` to create PRs
- Other workflows: Read-only access unless explicitly specified
