# SOEN 390 W26 Project

## Team Name
**The Null Pointers**

## Team Members

### Frontend Team
| Name | Student ID |
|-----|-----------|
| Shadi Marzouk | 27231466 |
| Anh Vi Mac | 40252504 |
| Hossam Khalifa | 40199572 |
| Adam Oughourlian | 40246313 |
| Abdeljalil Sennaoui | 40117162 |


### Backend Team
| Name | Student ID |
|-----|-----------|
| Hossam Mostafa | 40245337 |
| Kevin Ung | 40259218 |
| Samuel Vachon | 40281580 |
| Oscar Mirontsuk | 40191431 |
| Loucif Mohamed-Rabah-Ishaq | 40282580 |
| Omar Chabti | 40262497 |


## Project Overview
This project is a **Campus Guide application** designed to assist Concordia University students with navigation, scheduling, and points of interest across both **SGW** and **Loyola** campuses.

The system provides outdoor and indoor navigation, integrates with academic schedules, and supports accessibility focused routing. The application aims to improve the student experience by reducing confusion, wasted time, and accidental cardio.

## Core Features

### Campus Navigation
- Interactive campus maps for **SGW** and **Loyola**
- Clear distinction between campus buildings and surrounding city buildings
- Toggle button to switch between campuses
- Display of the building the user is currently located in
- Pop up information panels for buildings

### Outdoor Directions
- Select start and destination buildings by:
  - Clicking on buildings
  - Typing building names
- Use current location as the starting point
- Display routes using Google Maps API
- Support navigation between SGW and Loyola
- Multiple transportation modes:
  - Walking
  - Driving
  - Public transportation
- Support for the **Concordia Shuttle Service**
  - Time aware
  - Location aware

### Academic Schedule Integration
- Integration with **Google Calendar**
- Ability to select from multiple calendars
- Identify the next upcoming class
- Locate the classroom for the next scheduled course
- Generate directions to the next class based on the current time

> Note: Two alternative approaches will be researched:
> 1. Google Calendar integration  
> 2. Concordia Open Data API integration  
> Only one will be fully implemented, but both were evaluated for feasibility.

### Concordia Open Data Integration (Alternative Feature)
- Connect to Concordia Open Data API
- Retrieve course schedules and classroom locations
- Generate directions to the next class based on real time data

### Indoor Navigation (Critical Feature)
- Indoor maps with floor specific views
- Ability to locate specific rooms
- Shortest path indoor navigation
- Navigation between rooms on different floors
- Accessibility aware routing for students with disabilities
- Highlight indoor points of interest:
  - Washrooms
  - Water fountains
  - Stairs
  - Elevators
- Indoor navigation between buildings and campuses where applicable

### Outdoor Points of Interest
- Display nearby outdoor points of interest:
  - Restaurants
  - Coffee shops
  - Other amenities
- Search for nearest points of interest or by range
- Generate directions to selected locations

## Technologies
The full technology stack will be documented as development progresses, including frontend frameworks, backend services, APIs, and mapping tools.

## Setup Instructions

### Google Maps API Key Setup
1. Create a `local.properties` file in the project root directory (if it doesn't exist)
2. Add your Google Maps API key:
   ```properties
   MAPS_API_KEY=your_api_key_here
   ```
3. Save the file and sync Gradle

**Note:** The `local.properties` file is gitignored. Never commit API keys to the repository.

## Repository Structure
