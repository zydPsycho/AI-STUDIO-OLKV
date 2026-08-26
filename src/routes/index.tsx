import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useMemo, useState, type FormEvent, type ReactNode } from "react";
import { supabase } from "@/integrations/supabase/client";
import { Phone, Plus, Search, ShieldAlert, UserRound, HeartPulse, MapPin, RefreshCw, X, Camera, UsersRound, Info } from "lucide-react";

export const Route = createFileRoute("/")({ component: BloodLinkHome });

type Donor = {
  id: string;
  name: string;
  age: number;
  blood_group: string;
  phone: string;
  photo_url: string | null;
  is_available: boolean;
};

type FormState = {
  name: string;
  age: string;
  blood_group: string;
  phone: string;
  is_available: boolean;
  photoFile: File | null;
  photoPreview: string;
};

const GROUPS = ["All", "A+", "A−", "B+", "B−", "O+", "O−", "AB+", "AB−"];
const DB = supabase as any;

function initials(name: string) {
  return name.trim().split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join("").toUpperCase() || "?";
}

function emptyForm(): FormState {
  return { name: "", age: "", blood_group: "", phone: "", is_available: true, photoFile: null, photoPreview: "" };
}

function BloodLinkHome() {
  const [tab, setTab] = useState<"directory" | "profile" | "settings">("directory");
  const [donors, setDonors] = useState<Donor[]>([]);
  const [query, setQuery] = useState("");
  const [group, setGroup] = useState("All");
  const [selected, setSelected] = useState<Donor | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [myDonorId, setMyDonorId] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadDonors() {
    setLoading(true);
    setError("");
    const { data, error: readError } = await DB.from("kadu_donors").select("id,name,age,blood_group,phone,photo_url,is_available").order("created_at", { ascending: false }).limit(200);
    if (readError) setError("The KADU directory is temporarily unavailable. Please try again.");
    else setDonors((data || []) as Donor[]);
    setLoading(false);
  }

  useEffect(() => { void loadDonors(); }, []);
  useEffect(() => { setMyDonorId(window.localStorage.getItem("kadu-donor-id") || ""); }, []);

  const filtered = useMemo(() => donors.filter((donor) => {
    const textMatch = !query.trim() || donor.name.toLowerCase().includes(query.trim().toLowerCase());
    const groupMatch = group === "All" || donor.blood_group === group;
    return donor.id !== myDonorId && textMatch && groupMatch;
  }), [donors, group, query, myDonorId]);

  const myDonor = donors.find((donor) => donor.id === myDonorId);

  async function updateAvailability(value: boolean) {
    if (!myDonorId) return;
    setError("");
    const { data, error: updateError } = await DB.from("kadu_donors").update({ is_available: value }).eq("id", myDonorId).select("id,name,age,blood_group,phone,photo_url,is_available").single();
    if (updateError) {
      setError("We could not update your availability. Please try again.");
      return;
    }
    setDonors((current) => current.map((donor) => donor.id === myDonorId ? data as Donor : donor));
    setMessage(value ? "You are marked available in the KADU directory." : "You are marked not available in the KADU directory.");
  }

  function updateForm<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function choosePhoto(file: File | undefined) {
    if (!file) return;
    updateForm("photoFile", file);
    updateForm("photoPreview", URL.createObjectURL(file));
  }

  async function publishProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (myDonorId) {
      setMessage("Your profile is already published. Use Settings to update availability.");
      setTab("settings");
      return;
    }
    setError("");
    setMessage("");
    const age = Number(form.age);
    if (form.name.trim().length < 2 || !Number.isInteger(age) || age < 18 || age > 70 || !form.blood_group || form.phone.replace(/\D/g, "").length < 7) {
      setError("Please enter a name, age between 18 and 70, blood group, and valid phone number.");
      return;
    }
    setSaving(true);
    try {
      let photo_url: string | null = null;
      if (form.photoFile) {
        const safeName = form.photoFile.name.replace(/[^a-zA-Z0-9._-]/g, "-");
        const path = `${Date.now()}-${safeName}`;
        const upload = await supabase.storage.from("kadu-donor-photos").upload(path, form.photoFile, { upsert: false, contentType: form.photoFile.type || "image/jpeg" });
        if (upload.error) throw upload.error;
        photo_url = supabase.storage.from("kadu-donor-photos").getPublicUrl(path).data.publicUrl;
      }
      const { data, error: insertError } = await DB.from("kadu_donors").insert({ union_name: "KADU", name: form.name.trim(), age, blood_group: form.blood_group, phone: form.phone.trim(), photo_url, is_available: form.is_available }).select("id,name,age,blood_group,phone,photo_url,is_available").single();
      if (insertError) throw insertError;
      const donor = data as Donor;
      localStorage.setItem("kadu-donor-id", donor.id);
      setMyDonorId(donor.id);
      setDonors((current) => [donor, ...current]);
      setForm(emptyForm());
      setMessage("Your profile is now visible in the KADU directory.");
      setTab("profile");
    } catch (publishError) {
      console.error(publishError);
      setError("We could not publish your profile. Please check your connection and try again.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="min-h-screen bg-background text-foreground pb-24">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-5 py-5 lg:px-10">
        <button className="flex items-center gap-3 text-left" onClick={() => { setTab("directory"); window.scrollTo({ top: 0, behavior: "smooth" }); }}>
          <span className="grid size-11 place-items-center rounded-2xl bg-primary text-primary-foreground shadow-float"><HeartPulse className="size-6" /></span>
          <span><span className="block font-heading text-[15px] font-extrabold tracking-[0.14em] text-primary">BLOODLINK</span><span className="block text-[11px] font-semibold tracking-[0.14em] text-muted-foreground">by KADU</span></span>
        </button>
        <div className="hidden items-center gap-2 text-right text-xs text-muted-foreground sm:flex"><MapPin className="size-4 text-primary" /><span>Kavaratti, Lakshadweep</span></div>
        <button onClick={() => { setTab("profile"); window.scrollTo({ top: 0, behavior: "smooth" }); }} className="rounded-xl border border-border bg-surface px-3 py-2 text-xs font-bold text-primary shadow-card transition hover:-translate-y-0.5"><UserRound className="mr-1.5 inline size-4" />My profile</button>
      </header>

      <main className="mx-auto max-w-6xl px-5 lg:px-10">
        {tab === "directory" && <DirectoryView donors={donors} filtered={filtered} query={query} group={group} loading={loading} myDonor={myDonor} onQuery={setQuery} onGroup={setGroup} onRefresh={loadDonors} onSelect={setSelected} onCreate={() => { setTab("profile"); window.scrollTo({ top: 0, behavior: "smooth" }); }} onOpenMyProfile={() => { setTab("profile"); window.scrollTo({ top: 0, behavior: "smooth" }); }} />}
        {tab === "profile" && <ProfileViewV2 form={form} myDonor={myDonor} saving={saving} message={message} error={error} onForm={updateForm} onPhoto={choosePhoto} onSubmit={publishProfile} onBack={() => setTab("directory")} onOpenSettings={() => setTab("settings")} />}
        {tab === "settings" && <SettingsView donor={myDonor} message={message} error={error} onAvailabilityChange={updateAvailability} onBack={() => setTab("directory")} />}
      </main>

      <nav className="fixed inset-x-0 bottom-0 z-20 border-t border-border bg-background/95 px-3 py-2 backdrop-blur sm:hidden">
        <div className="mx-auto grid max-w-md grid-cols-3 gap-2">
          <NavButton active={tab === "directory"} icon={<UsersRound className="size-5" />} label="Donors" onClick={() => setTab("directory")} />
          <NavButton active={tab === "profile"} icon={<UserRound className="size-5" />} label="My profile" onClick={() => setTab("profile")} />
          <NavButton active={tab === "settings"} icon={<Info className="size-5" />} label="Settings" onClick={() => setTab("settings")} />
        </div>
      </nav>

      {selected && <DonorSheet donor={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}

function DirectoryView({ donors, filtered, query, group, loading, myDonor, onQuery, onGroup, onRefresh, onSelect, onCreate, onOpenMyProfile }: { donors: Donor[]; filtered: Donor[]; query: string; group: string; loading: boolean; myDonor?: Donor; onQuery: (value: string) => void; onGroup: (value: string) => void; onRefresh: () => void; onSelect: (donor: Donor) => void; onCreate: () => void; onOpenMyProfile: () => void }) {
  const available = filtered.filter((donor) => donor.is_available).length;
  return <div className="space-y-8 py-5 lg:py-10">
    <section className="grid gap-8 lg:grid-cols-[1.05fr_0.95fr] lg:items-end">
      <div><p className="mb-3 text-xs font-extrabold uppercase tracking-[0.2em] text-primary">KADU union directory</p><h1 className="max-w-2xl font-heading text-4xl font-extrabold leading-[1.04] tracking-tight sm:text-6xl">Find help close to home.</h1><p className="mt-5 max-w-xl text-base leading-7 text-muted-foreground sm:text-lg">Browse KADU donor members in Kavaratti by blood group. Names, ages, blood groups and phone numbers are available for union coordination.</p><button onClick={myDonor ? onOpenMyProfile : onCreate} className="mt-7 rounded-2xl bg-primary px-5 py-3.5 text-sm font-extrabold text-primary-foreground shadow-float transition hover:-translate-y-0.5">{myDonor ? <UserRound className="mr-2 inline size-4" /> : <Plus className="mr-2 inline size-4" />}{myDonor ? "My profile" : "Add donor profile"}</button></div>
      <div className="relative overflow-hidden rounded-[2rem] bg-primary p-7 text-primary-foreground shadow-float"><div className="absolute -right-10 -top-12 size-40 rounded-full bg-white/10" /><div className="absolute -bottom-16 left-12 size-48 rounded-full border-[18px] border-white/10" /><div className="relative"><p className="text-xs font-bold uppercase tracking-[0.16em] text-primary-foreground/70">BLOODLINK by KADU</p><p className="mt-3 max-w-sm font-heading text-2xl font-bold leading-tight">KADU members helping KADU members.</p><div className="mt-8 flex items-end gap-8"><div><p className="font-heading text-4xl font-extrabold">{donors.length}</p><p className="text-xs text-primary-foreground/70">total donors</p></div><div><p className="font-heading text-4xl font-extrabold">{donors.filter((donor) => donor.is_available).length}</p><p className="text-xs text-primary-foreground/70">available now</p></div></div></div></div>
    </section>

    <section className="rounded-[1.6rem] border border-border bg-surface p-4 shadow-card sm:p-5"><div className="flex flex-col gap-4 sm:flex-row"><label className="relative flex-1"><Search className="pointer-events-none absolute left-4 top-1/2 size-5 -translate-y-1/2 text-muted-foreground" /><input value={query} onChange={(event) => onQuery(event.target.value)} placeholder="Search by donor name" className="w-full rounded-2xl border border-input bg-background py-3.5 pl-12 pr-4 text-sm outline-none transition focus:border-primary focus:ring-4 focus:ring-primary/10" /></label><button onClick={onRefresh} className="rounded-2xl border border-border px-4 py-3 text-sm font-bold text-muted-foreground transition hover:border-primary hover:text-primary"><RefreshCw className="mr-2 inline size-4" />Refresh</button></div><div className="no-scrollbar mt-4 flex gap-2 overflow-x-auto pb-1">{GROUPS.map((item) => <button key={item} onClick={() => onGroup(item)} className={`shrink-0 rounded-full px-4 py-2 text-xs font-extrabold transition ${group === item ? "bg-primary text-primary-foreground" : "bg-primary-soft text-primary hover:bg-primary/15"}`}>{item}</button>)}</div></section>

    <section><div className="mb-4 flex items-center justify-between"><div><h2 className="font-heading text-2xl font-bold">Donors in Kavaratti</h2><p className="mt-1 text-sm text-muted-foreground">{filtered.length} shown · {available} available now</p></div><span className="hidden rounded-full bg-accent-soft px-3 py-2 text-xs font-bold text-accent-foreground sm:inline-flex"><span className="mr-2 size-2 rounded-full bg-accent" />KADU only</span></div>{loading ? <div className="rounded-3xl border border-border bg-surface p-12 text-center text-sm text-muted-foreground">Loading the shared directory…</div> : filtered.length === 0 ? <div className="rounded-3xl border border-dashed border-border bg-surface p-12 text-center"><HeartPulse className="mx-auto size-10 text-primary" /><h3 className="mt-4 font-heading text-xl font-bold">No matching donors</h3><p className="mt-2 text-sm text-muted-foreground">Try another blood group or add the first profile.</p><button onClick={myDonor ? onOpenMyProfile : onCreate} className="mt-5 rounded-xl bg-primary px-4 py-2.5 text-xs font-extrabold text-primary-foreground">{myDonor ? "MY PROFILE" : "ADD PROFILE"}</button></div> : <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">{filtered.map((donor) => <DonorCard key={donor.id} donor={donor} onClick={() => onSelect(donor)} />)}</div>}</section>

  </div>;
}

function DonorCard({ donor, onClick }: { donor: Donor; onClick: () => void }) { return <button onClick={onClick} className="group flex w-full items-center gap-4 rounded-3xl border border-border bg-surface p-4 text-left shadow-card transition hover:-translate-y-1 hover:border-primary/40 hover:shadow-float"><Avatar donor={donor} size="md" /><span className="min-w-0 flex-1"><span className="block truncate font-heading text-base font-bold">{donor.name}</span><span className="mt-1 block text-sm text-muted-foreground">{donor.age} years · Kavaratti</span><span className="mt-2 inline-flex items-center gap-1.5 text-xs font-bold text-accent-foreground"><span className={`size-2 rounded-full ${donor.is_available ? "bg-accent" : "bg-muted-foreground/50"}`} />{donor.is_available ? "Available now" : "Not available"}</span></span><span className="rounded-2xl bg-primary-soft px-3 py-2 font-heading text-lg font-extrabold text-primary">{donor.blood_group}</span></button>; }

function Avatar({ donor, size }: { donor: Donor; size: "md" | "lg" }) { return donor.photo_url ? <img src={donor.photo_url} alt={`Profile of ${donor.name}`} className={`${size === "lg" ? "size-20" : "size-14"} shrink-0 rounded-2xl object-cover`} /> : <span className={`${size === "lg" ? "size-20 text-xl" : "size-14 text-base"} grid shrink-0 place-items-center rounded-2xl bg-primary-soft font-heading font-extrabold text-primary`}>{initials(donor.name)}</span>; }

function DonorSheet({ donor, onClose }: { donor: Donor; onClose: () => void }) { return <div className="fixed inset-0 z-30 flex items-end justify-center bg-ink/35 p-0 backdrop-blur-sm sm:items-center sm:p-5" onClick={onClose}><div className="w-full max-w-lg rounded-t-[2rem] bg-surface p-6 shadow-float sm:rounded-[2rem]" onClick={(event) => event.stopPropagation()}><div className="flex items-start justify-between"><div className="flex items-center gap-4"><Avatar donor={donor} size="lg" /><div><h2 className="font-heading text-2xl font-extrabold">{donor.name}</h2><p className="text-sm text-muted-foreground">KADU · Kavaratti, Lakshadweep</p><p className="mt-2 text-xs font-bold text-accent-foreground"><span className={`mr-1.5 inline-block size-2 rounded-full ${donor.is_available ? "bg-accent" : "bg-muted-foreground/50"}`} />{donor.is_available ? "Available now" : "Not available"}</p></div></div><button onClick={onClose} className="rounded-full p-2 text-muted-foreground hover:bg-muted"><X className="size-5" /></button></div><div className="mt-6 divide-y divide-border rounded-2xl bg-background px-4"><div className="flex items-center justify-between py-4"><span className="text-sm text-muted-foreground">Blood group</span><span className="rounded-xl bg-primary-soft px-3 py-1.5 font-heading font-extrabold text-primary">{donor.blood_group}</span></div><div className="flex items-center justify-between py-4"><span className="text-sm text-muted-foreground">Age</span><span className="font-semibold">{donor.age} years</span></div><div className="flex items-center justify-between gap-6 py-4"><span className="text-sm text-muted-foreground">Phone number</span><span className="text-right font-semibold">{donor.phone}</span></div></div><a href={`tel:${donor.phone}`} className="mt-5 block rounded-2xl bg-primary px-5 py-4 text-center text-sm font-extrabold text-primary-foreground shadow-float"><Phone className="mr-2 inline size-4" />Call {donor.name}</a><p className="mt-4 text-center text-xs leading-5 text-muted-foreground">Phone numbers are visible because this is a KADU union directory. Please use contact details responsibly.</p></div></div>; }

function ProfileViewV2({ form, myDonor, saving, message, error, onForm, onPhoto, onSubmit, onBack, onOpenSettings }: { form: FormState; myDonor?: Donor; saving: boolean; message: string; error: string; onForm: <K extends keyof FormState>(key: K, value: FormState[K]) => void; onPhoto: (file: File | undefined) => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void; onBack: () => void; onOpenSettings: () => void }) {
  if (myDonor) return <div className="mx-auto max-w-3xl space-y-6 py-5 lg:py-10"><button onClick={onBack} className="text-sm font-bold text-primary">← Back to directory</button><div><p className="text-xs font-extrabold uppercase tracking-[0.2em] text-primary">KADU member profile</p><h1 className="mt-3 font-heading text-4xl font-extrabold tracking-tight sm:text-5xl">Your profile is live.</h1><p className="mt-4 max-w-xl text-base leading-7 text-muted-foreground">You can create one donor profile per device. Your own profile is hidden from your donor search results.</p></div><div className="rounded-[2rem] border border-border bg-surface p-5 shadow-card sm:p-7"><div className="flex items-center gap-4"><Avatar donor={myDonor} size="lg" /><div className="min-w-0 flex-1"><h2 className="font-heading text-2xl font-extrabold">{myDonor.name}</h2><p className="text-sm text-muted-foreground">{myDonor.age} years · {myDonor.blood_group} · {myDonor.phone}</p><p className="mt-2 text-xs font-bold text-accent-foreground">{myDonor.is_available ? "Available now" : "Not available"}</p></div></div><button onClick={onOpenSettings} className="mt-6 w-full rounded-2xl bg-primary px-5 py-4 text-sm font-extrabold text-primary-foreground shadow-float">UPDATE AVAILABILITY IN SETTINGS</button></div></div>;
  return <ProfileView form={form} myDonor={undefined} saving={saving} message={message} error={error} onForm={onForm} onPhoto={onPhoto} onSubmit={onSubmit} onBack={onBack} />;
}

function SettingsView({ donor, message, error, onAvailabilityChange, onBack }: { donor?: Donor; message: string; error: string; onAvailabilityChange: (value: boolean) => void; onBack: () => void }) { return <div className="mx-auto max-w-3xl space-y-7 py-5 lg:py-10"><button onClick={onBack} className="text-sm font-bold text-primary">← Back to directory</button><div><p className="text-xs font-extrabold uppercase tracking-[0.2em] text-primary">Settings</p><h1 className="mt-3 font-heading text-4xl font-extrabold tracking-tight sm:text-5xl">Your availability.</h1><p className="mt-4 max-w-xl text-base leading-7 text-muted-foreground">Only your locally saved profile can be changed from this device.</p></div>{donor ? <div className="rounded-[2rem] border border-border bg-surface p-5 shadow-card sm:p-7"><div className="flex items-center gap-4"><Avatar donor={donor} size="md" /><div><p className="font-heading text-lg font-bold">{donor.name}</p><p className="text-sm text-muted-foreground">{donor.blood_group} · {donor.phone}</p></div></div><label className="mt-6 flex items-center justify-between rounded-2xl bg-background px-4 py-4"><span><span className="block text-sm font-bold">Available to donate</span><span className="mt-1 block text-xs text-muted-foreground">Show your current status in the directory</span></span><input type="checkbox" checked={donor.is_available} onChange={(event) => onAvailabilityChange(event.target.checked)} className="size-5 accent-primary" /></label>{message && <p className="mt-4 rounded-xl bg-accent-soft px-4 py-3 text-sm font-semibold text-accent-foreground">{message}</p>}{error && <p className="mt-4 rounded-xl bg-destructive/10 px-4 py-3 text-sm font-semibold text-destructive">{error}</p>}</div> : <div className="rounded-3xl border border-dashed border-border bg-surface p-10 text-center"><h2 className="font-heading text-xl font-bold">Create your profile first</h2><p className="mt-2 text-sm text-muted-foreground">Your availability setting will appear here after you publish your KADU donor profile.</p></div>}<div className="flex items-start gap-3 rounded-3xl bg-accent-soft p-5 text-sm leading-6 text-accent-foreground"><ShieldAlert className="mt-1 size-5 shrink-0" /><p><strong>Union-use notice.</strong> This no-login directory should be shared only with KADU members. It is not a hospital blood bank or emergency service.</p></div></div>; }

function ProfileView({ form, myDonor, saving, message, error, onForm, onPhoto, onSubmit, onBack }: { form: FormState; myDonor?: Donor; saving: boolean; message: string; error: string; onForm: <K extends keyof FormState>(key: K, value: FormState[K]) => void; onPhoto: (file: File | undefined) => void; onSubmit: (event: FormEvent<HTMLFormElement>) => void; onBack: () => void }) { return <div className="mx-auto max-w-3xl space-y-7 py-5 lg:py-10"><button onClick={onBack} className="text-sm font-bold text-primary">← Back to directory</button><div><p className="text-xs font-extrabold uppercase tracking-[0.2em] text-primary">KADU member profile</p><h1 className="mt-3 font-heading text-4xl font-extrabold tracking-tight sm:text-5xl">Add your donor card.</h1><p className="mt-4 max-w-xl text-base leading-7 text-muted-foreground">Create a shared profile for the KADU union directory. No login is needed. Your phone number will be visible for union coordination.</p></div>{myDonor && <div className="flex items-center gap-4 rounded-3xl border border-border bg-surface p-4 shadow-card"><Avatar donor={myDonor} size="md" /><div className="min-w-0 flex-1"><p className="font-heading font-bold">Your profile is published</p><p className="text-sm text-muted-foreground">{myDonor.name} · {myDonor.blood_group} · {myDonor.phone}</p></div><span className="rounded-full bg-accent-soft px-3 py-1.5 text-xs font-bold text-accent-foreground">Live</span></div>}<form onSubmit={onSubmit} className="space-y-5 rounded-[2rem] border border-border bg-surface p-5 shadow-card sm:p-7"><label className="flex cursor-pointer flex-col items-center justify-center rounded-3xl border border-dashed border-primary/30 bg-primary-soft/40 px-5 py-7 text-center transition hover:bg-primary-soft"><span className="grid size-20 place-items-center overflow-hidden rounded-2xl bg-primary-soft font-heading text-xl font-extrabold text-primary">{form.photoPreview ? <img src={form.photoPreview} alt="Selected profile preview" className="size-full object-cover" /> : <Camera className="size-7" />}</span><span className="mt-3 text-sm font-bold text-primary">{form.photoPreview ? "Change profile photo" : "Add profile photo"}</span><span className="mt-1 text-xs text-muted-foreground">JPG, PNG or WebP</span><input type="file" accept="image/png,image/jpeg,image/webp" className="sr-only" onChange={(event) => onPhoto(event.target.files?.[0])} /></label><div className="grid gap-5 sm:grid-cols-[1.4fr_0.6fr]"><label className="block"><span className="mb-2 block text-xs font-bold uppercase tracking-wider text-muted-foreground">Full name</span><input required value={form.name} onChange={(event) => onForm("name", event.target.value)} placeholder="e.g. Aisha Koya" className="w-full rounded-2xl border border-input bg-background px-4 py-3.5 text-sm outline-none focus:border-primary focus:ring-4 focus:ring-primary/10" /></label><label className="block"><span className="mb-2 block text-xs font-bold uppercase tracking-wider text-muted-foreground">Age</span><input required type="number" min="18" max="70" value={form.age} onChange={(event) => onForm("age", event.target.value)} placeholder="28" className="w-full rounded-2xl border border-input bg-background px-4 py-3.5 text-sm outline-none focus:border-primary focus:ring-4 focus:ring-primary/10" /></label></div><div><span className="mb-2 block text-xs font-bold uppercase tracking-wider text-muted-foreground">Blood group</span><div className="flex flex-wrap gap-2">{GROUPS.slice(1).map((item) => <button type="button" key={item} onClick={() => onForm("blood_group", item)} className={`rounded-full px-4 py-2.5 text-xs font-extrabold transition ${form.blood_group === item ? "bg-primary text-primary-foreground" : "bg-primary-soft text-primary hover:bg-primary/15"}`}>{item}</button>)}</div></div><label className="block"><span className="mb-2 block text-xs font-bold uppercase tracking-wider text-muted-foreground">Phone number</span><input required type="tel" value={form.phone} onChange={(event) => onForm("phone", event.target.value)} placeholder="Your KADU contact number" className="w-full rounded-2xl border border-input bg-background px-4 py-3.5 text-sm outline-none focus:border-primary focus:ring-4 focus:ring-primary/10" /></label>{error && <p className="rounded-xl bg-destructive/10 px-4 py-3 text-sm font-semibold text-destructive">{error}</p>}{message && <p className="rounded-xl bg-accent-soft px-4 py-3 text-sm font-semibold text-accent-foreground">{message}</p>}<button disabled={saving} className="w-full rounded-2xl bg-primary px-5 py-4 text-sm font-extrabold text-primary-foreground shadow-float transition hover:-translate-y-0.5 disabled:cursor-wait disabled:opacity-60">{saving ? "PUBLISHING…" : "PUBLISH TO KADU DIRECTORY"}</button><p className="text-center text-xs leading-5 text-muted-foreground">By publishing, you confirm these details are intended for KADU union use.</p></form></div>; }

function AboutView() { return <div className="mx-auto max-w-3xl space-y-7 py-5 lg:py-10"><p className="text-xs font-extrabold uppercase tracking-[0.2em] text-primary">About the directory</p><h1 className="font-heading text-4xl font-extrabold tracking-tight sm:text-5xl">BLOODLINK by KADU.</h1><p className="max-w-2xl text-lg leading-8 text-muted-foreground">A simple shared directory for KADU union members in Kavaratti, Lakshadweep.</p><div className="grid gap-4 sm:grid-cols-3"><AboutCard number="01" title="Create" text="Add your name, age, blood group, phone number and profile photo." /><AboutCard number="02" title="Filter" text="Search the shared directory by blood group or donor name." /><AboutCard number="03" title="Connect" text="Open a donor profile and call directly when coordination is needed." /></div><div className="flex items-start gap-3 rounded-3xl bg-accent-soft p-5 text-sm leading-6 text-accent-foreground"><ShieldAlert className="mt-1 size-5 shrink-0" /><p><strong>Union-use notice.</strong> This release intentionally has no login. Anyone who receives the APK or public URL can view the directory, so distribute it only within KADU. This service is not a hospital blood bank or emergency service.</p></div></div>; }

function AboutCard({ number, title, text }: { number: string; title: string; text: string }) { return <div className="rounded-3xl border border-border bg-surface p-5 shadow-card"><span className="inline-flex rounded-full bg-primary-soft px-3 py-1.5 text-xs font-extrabold text-primary">{number}</span><h3 className="mt-4 font-heading text-lg font-bold">{title}</h3><p className="mt-2 text-sm leading-6 text-muted-foreground">{text}</p></div>; }

function NavButton({ active, icon, label, onClick }: { active: boolean; icon: ReactNode; label: string; onClick: () => void }) { return <button onClick={onClick} className={`flex flex-col items-center gap-1 rounded-xl px-2 py-1.5 text-[11px] font-bold ${active ? "text-primary" : "text-muted-foreground"}`}>{icon}{label}</button>; }
