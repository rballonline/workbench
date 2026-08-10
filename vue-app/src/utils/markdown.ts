import DOMPurify from 'dompurify'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

/**
 * Assistant replies are model-generated markdown, and markdown allows raw HTML
 * pass-through - `marked` alone would happily emit an inline `<img onerror=...>` from the
 * source text, so the parsed output is sanitized before it ever reaches `v-html`.
 */
export function renderMarkdown (source: string): string {
  return DOMPurify.sanitize(marked.parse(source, { async: false }))
}
