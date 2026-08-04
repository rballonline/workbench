import vuetify from 'eslint-config-vuetify'

// `vuetify()` resolves to the flat-config array; await it so the entries land at the
// top level instead of nested one array deep.
const base = await vuetify()

export default [
  ...base,
  {
    // Emitted by `vue-tsc --build` because tsconfig.node.json is a composite project.
    ignores: ['dist/**', 'vite.config.d.mts'],
  },
  {
    rules: {
      'space-before-function-paren': 'off',
    },
  },
  {
    // Pinia's options API is built on `this` inside plain object literals — the rule
    // has no way to tell that apart from a stray `this` and flags every action.
    files: ['src/stores/**/*.ts'],
    rules: {
      'unicorn/no-this-outside-of-class': 'off',
    },
  },
]
