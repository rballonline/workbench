# Design Tokens & Theme Configuration

This document describes the design token approach used in angular-app to ensure consistency and portability across potential framework ports (React, Svelte, etc.).

## Principles

- **Framework-agnostic**: Tokens are defined in CSS custom properties (variables) that work in any framework
- **Tailwind-based**: Primary styling uses Tailwind CSS utility classes
- **Configurable**: Override tokens in `src/styles.css` and `tailwind.config.js` without touching component code

## Token Categories

### Colors

Primary colors (override in `src/styles.css`):
```css
--color-primary: #2563eb;           /* Primary action color (blue-600) */
--color-primary-dark: #1e40af;      /* Dark variant (blue-700) */
--color-surface: #ffffff;            /* Background cards/containers */
--color-surface-alt: #f9fafb;        /* Alternative surface (gray-50) */
--color-text: #1f2937;               /* Primary text (gray-900) */
--color-text-secondary: #6b7280;     /* Secondary text (gray-500) */
--color-error: #ef4444;              /* Error state (red-500) */
--color-success: #10b981;            /* Success state (emerald-500) */
--color-warning: #f59e0b;            /* Warning state (amber-500) */
```

### Spacing

Tailwind scales (in `tailwind.config.js`):
- Use standard Tailwind spacing: `px-4`, `py-2`, `gap-4`, etc.
- Override theme if custom spacing needed

### Typography

Tailwind scales (in `tailwind.config.js`):
- Font sizes: `text-xs`, `text-sm`, `text-lg`, `text-2xl`, `text-3xl`
- Font weights: `font-normal`, `font-semibold`, `font-bold`
- Line height: included in Tailwind defaults

## Migration Guide for Ports

To port to React/Svelte/Vue:

1. **CSS custom properties** remain the same — copy `src/styles.css` to the new project
2. **Tailwind classes** remain the same — any framework using Tailwind will work identically
3. **Component patterns** differ by framework but the visual layer (spacing, colors, typography) transfers directly
4. **Design decisions** are documented here, not scattered in component code

## Customization Examples

### Changing primary color globally

```css
/* src/styles.css */
:root {
  --color-primary: #7c3aed;  /* Change to purple-600 */
  --color-primary-dark: #6d28d9;
}

/* Tailwind overrides */
/* tailwind.config.js */
theme: {
  extend: {
    colors: {
      'brand-primary': 'var(--color-primary)',
      'brand-primary-dark': 'var(--color-primary-dark)'
    }
  }
}
```

### Using tokens in components

```html
<!-- Always use Tailwind utility classes in templates -->
<button class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
  Save
</button>

<!-- Or reference CSS variables in custom CSS -->
<style>
  .custom-button {
    background-color: var(--color-primary);
  }
</style>
```

## Related Files

- `src/styles.css` — CSS custom property definitions
- `tailwind.config.js` — Tailwind configuration and theme overrides
- `src/app/components/**/*.ts` — Component templates use Tailwind classes
