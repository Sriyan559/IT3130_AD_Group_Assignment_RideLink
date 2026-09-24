# Collaborative Git Branching Strategy

In accordance with IT3130 criteria (LO4, I3):

## 1. Protected Branches
- main: Production-ready release branch. Only updated via pull requests after full integration verification.
- develop: Integration branch for combining feature releases.

## 2. Member Feature Branches
Each member develops their assigned service in dedicated feature branches:
- eature/account-service (Fernando B S C - IT24103775)
- eature/driver-vehicle-service (Rathnakoon D A - IT24300246)
- eature/ride-management-service (Herath H M S R - IT24103280)
- eature/fare-payment-service (Sanjeewa H.D.U.S - IT24101590)

## 3. Pull Request & Review Policy
- No direct commits to main.
- Every feature branch requires peer review and passing CI before merging into develop.
