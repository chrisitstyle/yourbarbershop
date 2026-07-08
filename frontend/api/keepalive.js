export default async function handler(req, res) {
  const authHeader = req.headers.authorization;

  if (authHeader !== `Bearer ${process.env.CRON_SECRET}`) {
    return res.status(401).json({ error: "Unauthorized" });
  }

  try {
    const response = await fetch(
      `${process.env.SUPABASE_URL}/storage/v1/object/list/${process.env.SUPABASE_BUCKET}`,
      {
        method: "POST",
        headers: {
          apikey: process.env.SUPABASE_ANON_KEY,
          Authorization: `Bearer ${process.env.SUPABASE_ANON_KEY}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          limit: 1,
          offset: 0,
        }),
      },
    );

    if (!response.ok) {
      const text = await response.text();
      return res.status(response.status).json({ error: text });
    }

    return res.status(200).json({ ok: true });
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
}
