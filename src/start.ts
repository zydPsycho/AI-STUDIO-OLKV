import { createMiddleware, createStart } from "@tanstack/react-start";

const errorMiddleware = createMiddleware().server(async ({ next }) => {
  try {
    return await next();
  } catch (error) {
    console.error("[BLOODLINK request]", error);
    return new Response(
      "<html><body><h1>BLOODLINK by KADU</h1><p>Temporarily unavailable. Please try again.</p></body></html>",
      { status: 500, headers: { "content-type": "text/html; charset=utf-8" } },
    );
  }
});

export const startInstance = createStart(() => ({
  requestMiddleware: [errorMiddleware],
}));
