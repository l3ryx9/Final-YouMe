/**
 * Store Zustand — Authentification
 */
import { create } from 'zustand';
import type { User } from '@domain/entities/User';

interface AuthState {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  isInitialized: boolean; // true après le premier onAuthStateChanged
  error: string | null;
  setUser: (user: User | null) => void;
  setLoading: (loading: boolean) => void;
  setInitialized: () => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  reset: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isLoading: false,
  isAuthenticated: false,
  isInitialized: false,
  error: null,

  setUser: (user) => set({ user, isAuthenticated: user !== null }),
  setLoading: (isLoading) => set({ isLoading }),
  setInitialized: () => set({ isInitialized: true }),
  setError: (error) => set({ error }),
  clearError: () => set({ error: null }),
  reset: () => set({ user: null, isAuthenticated: false, isInitialized: false, error: null, isLoading: false }),
}));
