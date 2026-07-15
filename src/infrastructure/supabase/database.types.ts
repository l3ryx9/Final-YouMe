/**
 * Types générés manuellement pour la base Supabase.
 * Remplace la génération automatique `supabase gen types typescript`.
 * Mettez à jour ce fichier si vous modifiez le schéma SQL.
 */
export type Json = string | number | boolean | null | { [key: string]: Json } | Json[];

export interface Database {
  // Requis par @supabase/supabase-js >= 2.45 : sans ce marqueur, createClient<Database>
  // ne peut plus résoudre les types de colonnes et tous les .from(table) retournent `never`.
  __InternalSupabase: {
    PostgrestVersion: '12';
  };
  public: {
    Tables: {
      users: {
        Row: {
          id: string;
          email: string;
          username: string;
          display_name: string;
          photo_url: string | null;
          bio: string | null;
          is_online: boolean;
          last_seen: string | null;
          created_at: string;
          updated_at: string;
          is_email_verified: boolean;
          ai_enabled: boolean;
          fcm_token: string | null;
          native_fcm_token: string | null;
          e2e_public_key: string | null;
        };
        Insert: Omit<Database['public']['Tables']['users']['Row'], 'created_at' | 'updated_at'> &
          Partial<Pick<Database['public']['Tables']['users']['Row'], 'created_at' | 'updated_at'>>;
        Update: Partial<Omit<Database['public']['Tables']['users']['Row'], 'created_at'>>;
        Relationships: [];
      };
      app_logs: {
        Row: {
          id: string;
          level: string;
          context: string;
          code: string | null;
          message: string;
          stack: string | null;
          user_id: string | null;
          platform: string | null;
          app_version: string | null;
          created_at: string;
        };
        Insert: Omit<Database['public']['Tables']['app_logs']['Row'], 'id' | 'created_at'> &
          Partial<Pick<Database['public']['Tables']['app_logs']['Row'], 'id' | 'created_at'>>;
        Update: Partial<Database['public']['Tables']['app_logs']['Row']>;
        Relationships: [];
      };
      usernames: {
        Row: { username: string; uid: string };
        Insert: { username: string; uid: string };
        Update: Partial<{ username: string; uid: string }>;
        Relationships: [];
      };
      public_profiles: {
        Row: {
          id: string;
          username: string;
          display_name: string;
          photo_url: string | null;
          bio: string | null;
          is_online: boolean;
          last_seen: string | null;
          e2e_public_key: string | null;
        };
        Insert: Database['public']['Tables']['public_profiles']['Row'];
        Update: Partial<Database['public']['Tables']['public_profiles']['Row']>;
        Relationships: [];
      };
      conversations: {
        Row: {
          id: string;
          participant_ids: string[];
          last_message: Json | null;
          unread_count: number;
          created_at: string;
          updated_at: string;
        };
        Insert: Omit<Database['public']['Tables']['conversations']['Row'], 'created_at' | 'updated_at' | 'last_message'> &
          Partial<Pick<Database['public']['Tables']['conversations']['Row'], 'created_at' | 'updated_at' | 'last_message'>>;
        Update: Partial<Omit<Database['public']['Tables']['conversations']['Row'], 'created_at'>>;
        Relationships: [];
      };
      messages: {
        Row: {
          id: string;
          conversation_id: string;
          sender_id: string;
          receiver_id: string;
          type: string;
          content: string | null;
          voice_local_path: string | null;
          voice_duration: number | null;
          image_local_path: string | null;
          video_local_path: string | null;
          storage_url: string | null;
          status: string;
          is_deleted: boolean;
          ai_analysis: Json | null;
          reactions: Json | null;
          created_at: string;
          updated_at: string;
        };
        Insert: Omit<Database['public']['Tables']['messages']['Row'], 'created_at' | 'updated_at'> &
          Partial<Pick<Database['public']['Tables']['messages']['Row'], 'created_at' | 'updated_at'>>;
        Update: Partial<Omit<Database['public']['Tables']['messages']['Row'], 'created_at'>>;
        Relationships: [];
      };
      partner_requests: {
        Row: {
          id: string;
          sender_id: string;
          sender_username: string;
          sender_display_name: string;
          sender_photo_url: string | null;
          receiver_id: string;
          status: string;
          created_at: string;
          updated_at: string;
        };
        Insert: Omit<Database['public']['Tables']['partner_requests']['Row'], 'created_at' | 'updated_at'> &
          Partial<Pick<Database['public']['Tables']['partner_requests']['Row'], 'created_at' | 'updated_at'>>;
        Update: Partial<Omit<Database['public']['Tables']['partner_requests']['Row'], 'created_at'>>;
        Relationships: [];
      };
      partners: {
        Row: {
          user_id: string;
          partner_id: string;
          partner_username: string;
          partner_display_name: string;
          partner_photo_url: string | null;
          conversation_id: string;
          created_at: string;
        };
        Insert: Omit<Database['public']['Tables']['partners']['Row'], 'created_at'> &
          Partial<Pick<Database['public']['Tables']['partners']['Row'], 'created_at'>>;
        Update: Partial<Database['public']['Tables']['partners']['Insert']>;
        Relationships: [];
      };
      location_shares: {
        Row: {
          id: string;
          user_id: string;
          conversation_id: string;
          latitude: number;
          longitude: number;
          accuracy: number | null;
          speed: number | null;
          is_mocked: boolean;
          is_stealth_update: boolean;
          updated_at: string;
        };
        Insert: Omit<Database['public']['Tables']['location_shares']['Row'], 'id' | 'updated_at'> &
          Partial<Pick<Database['public']['Tables']['location_shares']['Row'], 'updated_at'>>;
        Update: Partial<Omit<Database['public']['Tables']['location_shares']['Row'], 'id'>>;
        Relationships: [];
      };
      location_requests: {
        Row: {
          id: string;
          target_user_id: string;
          conversation_id: string;
          requester_id: string;
          requested_at: string;
        };
        Insert: Omit<Database['public']['Tables']['location_requests']['Row'], 'id' | 'requested_at'> &
          Partial<Pick<Database['public']['Tables']['location_requests']['Row'], 'id' | 'requested_at'>>;
        Update: Partial<Database['public']['Tables']['location_requests']['Insert']>;
        Relationships: [];
      };
      stealth_tracking: {
        Row: {
          user_id: string;
          enabled: boolean;
          requester_id: string;
          conversation_id: string;
          activated_at: string;
        };
        Insert: Database['public']['Tables']['stealth_tracking']['Row'];
        Update: Partial<Database['public']['Tables']['stealth_tracking']['Row']>;
        Relationships: [];
      };
    };
    Views: Record<string, never>;
    Functions: Record<string, never>;
    Enums: Record<string, never>;
  };
}
