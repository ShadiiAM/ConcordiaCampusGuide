# Contributing to Campus Guide

Thank you for contributing to the Campus Guide project! This document outlines the guidelines and workflows for contributing to this repository.

## Table of Contents
- [Branch Strategy](#branch-strategy)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Code Style](#code-style)
- [Testing Requirements](#testing-requirements)

## Branch Strategy

We use a trunk-based development strategy with feature branches:

### Main Branch
- `main` - Production-ready code. Protected branch.

### Supporting Branches
- `feature/*` - New features (e.g., `feature/indoor-navigation`)
- `bugfix/*` - Bug fixes (e.g., `bugfix/map-loading-issue`)
- `hotfix/*` - Urgent production fixes (e.g., `hotfix/crash-on-startup`)

### Branch Naming Convention
```
<type>/<short-description>

Examples:
feature/epic1-campus-map
feature/epic2-outdoor-directions
bugfix/fix-building-popup
hotfix/api-key-exposure
```

## Commit Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

### Format
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Code style changes (formatting, etc.)
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Maintenance tasks
- `ci` - CI/CD changes

### Examples
```
feat(map): add SGW campus building overlay
fix(navigation): resolve route calculation error
docs(readme): update setup instructions
test(directions): add unit tests for route service
```

## Pull Request Process

1. **Create a branch** from `main` following the naming convention
2. **Make your changes** with appropriate commits
3. **Push your branch** to the remote repository
4. **Open a Pull Request** to `main`
5. **Fill out the PR template** completely
6. **Request reviews** from at least one team member
7. **Address feedback** and make necessary changes
8. **Merge** once approved and CI passes

### PR Requirements
- All CI checks must pass
- At least 1 approval required
- No unresolved conversations
- Branch must be up-to-date with target branch

## Code Style

### Kotlin
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Document public APIs with KDoc

### XML (Layouts)
- Use `snake_case` for resource IDs
- Prefix IDs with view type (e.g., `btn_submit`, `tv_title`, `rv_buildings`)
- Use `@string` resources for all user-facing text

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/campusguide/
│   │   │   ├── data/          # Data layer (repositories, data sources)
│   │   │   ├── domain/        # Domain layer (use cases, models)
│   │   │   ├── presentation/  # UI layer (activities, fragments, viewmodels)
│   │   │   └── di/            # Dependency injection
│   │   └── res/
│   └── test/                  # Unit tests
└── build.gradle.kts
```

## Testing Requirements

### Unit Tests
- Write unit tests for all business logic
- Aim for meaningful coverage of critical paths
- Use descriptive test names

### Test Naming Convention
```kotlin
@Test
fun `methodName should expectedBehavior when condition`() {
    // Given
    // When
    // Then
}
```

### Running Tests
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run tests with coverage
./gradlew testDebugUnitTestCoverage
```

## Getting Help

- Check existing issues and PRs before creating new ones
- Use GitHub Discussions for questions
- Tag relevant team members in your PR

## Team Contacts

### Frontend Team
- Shadi Marzouk, Omar Chabti, Samuel Vachon, Abdeljalil Sennaoui, Kevin Ung

### Backend Team
- Anh Vi Mac, Hossam Mostafa, Hossam Khalifa, Adam Oughourlian, Oscar Mirontsuk, Loucif Mohamed-Rabah-Ishaq
