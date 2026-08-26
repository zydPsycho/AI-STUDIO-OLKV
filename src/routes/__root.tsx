import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  HeadContent,
  Outlet,
  Scripts,
  createRootRouteWithContext,
} from "@tanstack/react-router";
import type { ReactNode } from "react";

import appCss from "../styles.css?url";

function NotFoundComponent() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="max-w-md text-center">
        <p className="text-xs font-extrabold uppercase tracking-[0.2em] text-primary">BLOODLINK by KADU</p>
        <h1 className="mt-4 font-heading text-7xl font-bold text-primary">404</h1>
        <h2 className="mt-4 font-heading text-xl font-semibold">Page not found</h2>
        <p className="mt-2 text-sm text-muted-foreground">Return to the KADU donor directory.</p>
        <a href="/" className="mt-6 inline-flex items-center justify-center rounded-xl bg-primary px-5 py-2.5 text-sm font-semibold text-primary-foreground shadow-float">Back to BLOODLINK</a>
      </div>
    </div>
  );
}

function ErrorComponent({ reset }: { error: Error; reset: () => void }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="max-w-md text-center">
        <p className="text-xs font-extrabold uppercase tracking-[0.2em] text-primary">BLOODLINK by KADU</p>
        <h1 className="mt-4 font-heading text-xl font-semibold">Something went wrong</h1>
        <p className="mt-2 text-sm text-muted-foreground">Try again or return to the donor directory.</p>
        <button onClick={reset} className="mt-6 rounded-xl bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground">Try again</button>
      </div>
    </div>
  );
}

export const Route = createRootRouteWithContext<{ queryClient: QueryClient }>()({
  head: () => ({
    meta: [
      { charSet: "utf-8" },
      { name: "viewport", content: "width=device-width, initial-scale=1, viewport-fit=cover" },
      { name: "theme-color", content: "#8F2D32" },
      { title: "BLOODLINK by KADU — Kavaratti" },
      { name: "description", content: "BLOODLINK by KADU is the shared blood donor directory for KADU union members in Kavaratti, Lakshadweep." },
      { name: "author", content: "KADU" },
      { property: "og:title", content: "BLOODLINK by KADU — Find help close to home" },
      { property: "og:description", content: "A shared KADU union donor directory for Kavaratti, Lakshadweep." },
      { property: "og:type", content: "website" },
      { property: "og:site_name", content: "BLOODLINK by KADU" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
    links: [
      { rel: "stylesheet", href: appCss },
      { rel: "icon", href: "/favicon.svg", type: "image/svg+xml" },
      { rel: "preconnect", href: "https://fonts.googleapis.com" },
      { rel: "preconnect", href: "https://fonts.gstatic.com", crossOrigin: "anonymous" },
      { rel: "stylesheet", href: "https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&family=Figtree:wght@300;400;500;600;700&display=swap" },
    ],
  }),
  shellComponent: RootShell,
  component: RootComponent,
  notFoundComponent: NotFoundComponent,
  errorComponent: ErrorComponent,
});

function RootShell({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <head><HeadContent /></head>
      <body className="antialiased">{children}<Scripts /></body>
    </html>
  );
}

function RootComponent() {
  const { queryClient } = Route.useRouteContext();
  return <QueryClientProvider client={queryClient}><Outlet /></QueryClientProvider>;
}
