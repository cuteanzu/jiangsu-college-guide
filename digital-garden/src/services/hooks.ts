import { useState, useEffect, useCallback } from "react";
import api from "./api";
import type { University, Tier } from "../data/jiangsu-universities";
import type { ExperiencePost, QAEntry } from "../data/mock-content";

// ── Types matching backend responses ──

export interface CityProfile {
  id: number;
  name: string;
  schoolCount: number;
  tags: string[];
  cost: string;
  transit: string;
  jobs: string;
  audience: string;
}

// ── Generic async state ──

interface AsyncState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

function useAsync<T>(fetcher: () => Promise<T>, deps: unknown[] = []): AsyncState<T> & { refetch: () => void } {
  const [state, setState] = useState<AsyncState<T>>({ data: null, loading: true, error: null });

  const fetch = useCallback(() => {
    setState((s) => ({ ...s, loading: true, error: null }));
    fetcher()
      .then((data) => setState({ data, loading: false, error: null }))
      .catch((err) => setState({ data: null, loading: false, error: err.message ?? "加载失败" }));
  }, deps);

  useEffect(() => { fetch(); }, [fetch]);

  return { ...state, refetch: fetch };
}

// ── University hooks ──

export function useUniversities() {
  return useAsync<University[]>(async () => {
    const { data } = await api.get<any[]>("/schools/universities");
    return data.map((u: any) => ({
      id: u.id,
      name: u.name,
      city: u.city,
      lat: u.lat,
      lng: u.lng,
      tier: u.tier as Tier,
      founded: u.founded,
      website: u.website,
    }));
  }, []);
}

export function useUniversityByCode(code: string | null) {
  return useAsync<University | null>(async () => {
    if (!code) return null;
    const { data } = await api.get<any>(`/schools/universities/${code}`);
    return {
      id: data.id,
      name: data.name,
      city: data.city,
      lat: data.lat,
      lng: data.lng,
      tier: data.tier as Tier,
      founded: data.founded,
      website: data.website,
    };
  }, [code]);
}

// ── Experience hooks ──

export function useExperiences(category?: string, city?: string, schoolId?: string) {
  return useAsync<ExperiencePost[]>(async () => {
    const params = new URLSearchParams();
    if (category) params.set("category", category);
    if (city) params.set("city", city);
    if (schoolId) params.set("schoolId", schoolId);
    const { data } = await api.get<any[]>(`/experiences?${params.toString()}`);
    return data.map((e: any) => ({
      id: e.id,
      category: e.category,
      schoolId: e.schoolId ?? "",
      schoolName: e.schoolName ?? "",
      city: e.city ?? "",
      title: e.title,
      excerpt: e.excerpt ?? "",
      body: e.body,
      likes: e.likes ?? 0,
      comments: e.comments ?? 0,
      tags: e.tags ?? [],
    }));
  }, [category, city, schoolId]);
}

// ── QA hooks ──

export function useQA(category?: string) {
  return useAsync<QAEntry[]>(async () => {
    const params = category ? `?category=${encodeURIComponent(category)}` : "";
    const { data } = await api.get<any[]>(`/qa${params}`);
    return data.map((q: any) => ({
      id: q.id,
      question: q.question,
      answer: q.answer,
      schoolId: q.schoolId,
      schoolName: q.schoolName,
      category: q.category,
      likes: q.likes ?? 0,
    }));
  }, [category]);
}

// ── City profile hooks ──

export function useCityProfiles() {
  return useAsync<CityProfile[]>(async () => {
    const { data } = await api.get<any[]>("/cities/profiles");
    return data;
  }, []);
}

// ── Auth hooks ──

export function useCurrentUser() {
  return useAsync<any>(async () => {
    const token = localStorage.getItem("token");
    if (!token) return null;
    const { data } = await api.get("/auth/me");
    return data;
  }, []);
}

// ── Combined map data hook ──

export function useMapData() {
  const universities = useUniversities();
  const cityProfiles = useCityProfiles();
  const experiences = useExperiences();
  const qaEntries = useQA();

  return {
    universities,
    cityProfiles,
    experiences,
    qaEntries,
    loading:
      universities.loading || cityProfiles.loading || experiences.loading || qaEntries.loading,
    error: universities.error || cityProfiles.error || experiences.error || qaEntries.error,
  };
}
