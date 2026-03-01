import type * as T from './type'
import http from '@/utils/http'

const BASE_URL = '/template/generate'

export function sendChatMessage(data: T.ChatRequest) {
  return http.post<T.ChatResponse>(`${BASE_URL}/chat`, data)
}

export function listSessions() {
  return http.get<T.ChatSession[]>(`${BASE_URL}/session/list`)
}

export function createSession() {
  return http.post<T.ChatSession>(`${BASE_URL}/session`)
}

export function getSessionMessages(sessionId: number) {
  return http.get<T.ChatMessage[]>(`${BASE_URL}/session/${sessionId}/messages`)
}

export function deleteSession(sessionId: number) {
  return http.del(`${BASE_URL}/session/${sessionId}`)
}

export function getSessionSnippets(sessionId: number) {
  return http.get<T.CodeSnippet[]>(`${BASE_URL}/session/${sessionId}/snippets`)
}

export function updateSnippet(snippetId: number, code: string) {
  return http.put(`${BASE_URL}/snippet/${snippetId}`, { code })
}

export function saveSnippetToLibrary(snippetId: number, data?: T.SaveToLibraryReq) {
  return http.post<number>(`${BASE_URL}/snippet/${snippetId}/save-to-library`, data)
}

export function startResearch(data: T.ResearchStartReq) {
  return http.post<T.ResearchStatus>(`${BASE_URL}/research/start`, data)
}

export function subscribeResearchProgress(taskId: string): EventSource {
  return new EventSource(`${BASE_URL}/research/${taskId}/progress`)
}

export function cancelResearch(taskId: string) {
  return http.del(`${BASE_URL}/research/${taskId}`)
}

export function submitResearchFeedback(taskId: string, feedback: string) {
  return http.post(`${BASE_URL}/research/${taskId}/feedback`, { feedback })
}

export function confirmResearch(taskId: string) {
  return http.post(`${BASE_URL}/research/${taskId}/confirm`)
}
