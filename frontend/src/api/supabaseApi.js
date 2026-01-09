import { createClient } from "@supabase/supabase-js";

export const projectURL = import.meta.env.VITE_SUPABASE_PROJECTURL;
export const publicApiKey = import.meta.env.VITE_SUPABASE_PUBLICAPIKEY;
export const CDNURL = import.meta.env.VITE_SUPABASE_CDNURL;

export const supabase = createClient(projectURL, publicApiKey);

export function useSupabaseClient() {
  return supabase;
}
