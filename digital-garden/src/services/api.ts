import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

// Request interceptor — attach JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor — unwrap Result.data, handle errors
api.interceptors.response.use(
  (response) => {
    // Backend wraps all responses in Result<T> { code, message, data }
    const body = response.data;
    if (body && typeof body === "object" && "code" in body && "data" in body) {
      if (body.code === 200) {
        return { ...response, data: body.data };
      }
      return Promise.reject(new Error(body.message ?? "Request failed"));
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem("token");
      localStorage.removeItem("refreshToken");
    }
    return Promise.reject(error);
  },
);

export default api;
