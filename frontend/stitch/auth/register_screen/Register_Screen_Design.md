---
name: Service Professionalism
colors:
  surface: '#f9f9ff'
  surface-dim: '#cfdaf2'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f3ff'
  surface-container: '#e7eeff'
  surface-container-high: '#dee8ff'
  surface-container-highest: '#d8e3fb'
  on-surface: '#111c2d'
  on-surface-variant: '#424656'
  inverse-surface: '#263143'
  inverse-on-surface: '#ecf1ff'
  outline: '#727687'
  outline-variant: '#c2c6d8'
  surface-tint: '#0054d6'
  primary: '#0050cb'
  on-primary: '#ffffff'
  primary-container: '#0066ff'
  on-primary-container: '#f8f7ff'
  inverse-primary: '#b3c5ff'
  secondary: '#4648d4'
  on-secondary: '#ffffff'
  secondary-container: '#6063ee'
  on-secondary-container: '#fffbff'
  tertiary: '#565a5b'
  on-tertiary: '#ffffff'
  tertiary-container: '#6f7274'
  on-tertiary-container: '#f6f8fa'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae1ff'
  primary-fixed-dim: '#b3c5ff'
  on-primary-fixed: '#001849'
  on-primary-fixed-variant: '#003fa4'
  secondary-fixed: '#e1e0ff'
  secondary-fixed-dim: '#c0c1ff'
  on-secondary-fixed: '#07006c'
  on-secondary-fixed-variant: '#2f2ebe'
  tertiary-fixed: '#e0e3e5'
  tertiary-fixed-dim: '#c4c7c9'
  on-tertiary-fixed: '#191c1e'
  on-tertiary-fixed-variant: '#444749'
  background: '#f9f9ff'
  on-background: '#111c2d'
  surface-variant: '#d8e3fb'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 40px
  xl: 64px
  container-max: 1280px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style

The brand personality for the design system is anchored in reliability, efficiency, and human connection. It targets service providers and clients who value clarity and professional integrity. 

The aesthetic follows a **Corporate Modern** style with strong **Minimalist** influences. By prioritizing generous whitespace and a rigorous grid, the UI evokes an emotional response of organized calm and institutional trust. Visual clutter is eliminated to ensure that service listings and transactional data remain the primary focus. Every element is designed to feel intentional, accessible, and high-quality, reinforcing the platform's role as a dependable intermediary.

## Colors

The palette is dominated by "Reliable Blue," a high-energy yet professional primary hue that signals competence. 

- **Primary:** Used for main actions and branding elements.
- **Secondary:** An indigo shade used for accents and secondary interactive elements to provide depth.
- **Neutral:** A deep slate for typography to ensure high contrast and readability (meeting WCAG AA standards).
- **Surface:** The background uses off-white and cool grays to prevent eye strain.
- **Semantic:** Explicit success (green), error (red), and warning (amber) colors are utilized for real-time feedback and status indicators.

## Typography

The design system utilizes **Inter** exclusively to leverage its exceptional legibility and systematic feel. The type scale is built on a 1.25x ratio (Major Third) to create a clear hierarchy.

- **Headlines:** Use tighter letter-spacing and heavier weights to feel authoritative. Large display sizes are scaled down for mobile to ensure readability without excessive scrolling.
- **Body:** Uses a standard weight and generous line height to facilitate long-form reading in service descriptions.
- **Labels:** Employ medium weights and slight tracking for UI metadata, buttons, and micro-copy.

## Layout & Spacing

The design system employs a **Fluid Grid** model based on an 8px square system. This ensures mathematical harmony across all components and screen sizes.

- **Desktop (1280px+):** A 12-column grid with 24px gutters and 32px outer margins.
- **Tablet (768px - 1279px):** An 8-column grid with 20px gutters and 24px margins.
- **Mobile (Up to 767px):** A 4-column grid with 16px gutters and 16px margins.

Spacing should be applied using the defined increments (8, 16, 24, 40, 64) to maintain vertical rhythm. Layouts should prioritize top-aligned content and clear grouping of related service information.

## Elevation & Depth

This design system uses **Ambient Shadows** and **Tonal Layers** to establish hierarchy. The approach is subtle, avoiding heavy shadows to maintain a clean, modern appearance.

- **Level 0 (Flat):** Used for background surfaces.
- **Level 1 (Subtle):** A 1px border (#E2E8F0) with no shadow, used for secondary cards and static containers.
- **Level 2 (Raised):** A soft, diffused shadow (0px 4px 6px -1px rgba(0, 0, 0, 0.1)) used for interactive cards and floating elements like search bars.
- **Level 3 (Overlay):** A more pronounced shadow (0px 10px 15px -3px rgba(0, 0, 0, 0.1)) reserved for modals and dropdown menus.

Depth is also communicated through "Surface Containers"—using the tertiary color (#F8FAFC) to distinguish between the page canvas and content sections.

## Shapes

The shape language is defined as **Rounded**, utilizing a base corner radius of 8px (0.5rem). 

- **Components (Buttons, Inputs):** 8px radius.
- **Large Components (Cards, Containers):** 16px (1rem) radius.
- **Extra Large (Modals, Feature Blocks):** 24px (1.5rem) radius.

This roundedness balances professional structure with a friendly, accessible feel. Icons should follow this logic, utilizing rounded caps and joins to match the UI's geometry.

## Components

### Buttons
- **Primary:** Solid #0066FF background with white text. High-contrast and bold.
- **Secondary:** #0066FF border (2px) with #0066FF text.
- **Ghost:** Transparent background with Slate text for low-priority actions.
- **States:** Hover states should darken the primary color by 10%. Active states should include a 2px offset focus ring.

### Input Fields
- **Default:** White background, 1px border (#CBD5E1), 8px radius.
- **Focus:** 2px border using Primary Blue.
- **Validation:** 
  - **Success:** Border turns to Success Green with a trailing checkmark icon.
  - **Error:** Border turns to Error Red with a trailing alert icon and red helper text below the field.

### Cards
- **Service Card:** White background, Level 2 elevation. Contains 16px internal padding. Images should have top-only rounded corners (8px).

### Chips & Badges
- **Status Badges:** Subtle background tints (e.g., light green for 'Active') with high-contrast text. 100px (Pill) roundedness.

### Lists
- Use 16px vertical padding for list items with a 1px bottom divider (#F1F5F9). Icons should be monochromatic to maintain focus on content.

### Additional Components
- **Progress Steppers:** Used for service booking workflows to reduce cognitive load.
- **Service Tags:** Small, low-contrast chips used to categorize skills or services.