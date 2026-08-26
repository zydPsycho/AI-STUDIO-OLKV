export type Json = string | number | boolean | null | { [key: string]: Json | undefined } | Json[];

export type Database = {
  public: {
    Tables: {
      kadu_donors: {
        Row: {
          id: string;
          union_name: string;
          name: string;
          age: number;
          blood_group: string;
          phone: string;
          photo_url: string | null;
          is_available: boolean;
          is_active: boolean;
          created_at: string;
          updated_at: string;
        };
        Insert: {
          id?: string;
          union_name?: string;
          name: string;
          age: number;
          blood_group: string;
          phone: string;
          photo_url?: string | null;
          is_available?: boolean;
          is_active?: boolean;
          created_at?: string;
          updated_at?: string;
        };
        Update: Partial<Database["public"]["Tables"]["kadu_donors"]["Insert"]>;
        Relationships: [];
      };
      kadu_emergency_alerts: {
        Row: {
          id: string;
          union_name: string;
          sender_name: string;
          sender_phone: string;
          patient_name: string;
          admitted_in: string;
          emergency_type: string;
          required_blood_group: string;
          units_needed: number;
          notes: string | null;
          is_active: boolean;
          created_at: string;
        };
        Insert: {
          id?: string;
          union_name?: string;
          sender_name: string;
          sender_phone: string;
          patient_name: string;
          admitted_in: string;
          emergency_type: string;
          required_blood_group: string;
          units_needed?: number;
          notes?: string | null;
          is_active?: boolean;
          created_at?: string;
        };
        Update: Partial<Database["public"]["Tables"]["kadu_emergency_alerts"]["Insert"]>;
        Relationships: [];
      };
      kadu_push_tokens: {
        Row: {
          token: string;
          platform: "android" | "web";
          donor_id: string | null;
          last_seen_at: string;
          created_at: string;
        };
        Insert: {
          token: string;
          platform?: "android" | "web";
          donor_id?: string | null;
          last_seen_at?: string;
          created_at?: string;
        };
        Update: Partial<Database["public"]["Tables"]["kadu_push_tokens"]["Insert"]>;
        Relationships: [];
      };
    };
    Views: Record<string, never>;
    Functions: Record<string, never>;
    Enums: Record<string, never>;
    CompositeTypes: Record<string, never>;
  };
};
