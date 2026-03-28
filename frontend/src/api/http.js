import axios from "axios";

const api = axios.create({
  baseURL: "/api/gym",
  timeout: 60000
});

let tokenGetter = () => "";

export function setTokenGetter(fn) {
  tokenGetter = fn;
}

api.interceptors.request.use((config) => {
  const t = tokenGetter();
  if (t) config.headers.Authorization = `Bearer ${t}`;
  return config;
});

export default api;
