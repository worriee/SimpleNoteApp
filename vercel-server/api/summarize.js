export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res
      .status(405)
      .json({ error: "Method Not Allowed. Please use POST." });
  }

  try {
    const { url } = req.body;
    if (!url) {
      return res.status(400).json({ error: "YouTube URL is required." });
    }

    // 1. Extract Video ID
    const videoIdPattern =
      /(?<=watch\?v=|\/videos\/|embed\/|youtu\.be\/|\/v\/|\/e\/|watch\?v%3D|watch\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu\.be%2F|\/v\/)[^#&?\\n]*/;
    const match = url.match(videoIdPattern);
    if (!match) {
      return res.status(400).json({ error: "Invalid YouTube URL." });
    }
    const videoId = match[0];

    // 2. Fetch Captions from RapidAPI
    const captionApiKey = process.env.CAPTION_API_KEY;
    const captionApiUrl = `https://youtube-captions-transcript-subtitles-video-combiner.p.rapidapi.com/download-all/${videoId}?format_subtitle=srt&format_answer=json`;

    const captionResponse = await fetch(captionApiUrl, {
      method: "GET",
      headers: {
        "x-rapidapi-key": captionApiKey,
        "x-rapidapi-host":
          "youtube-captions-transcript-subtitles-video-combiner.p.rapidapi.com",
      },
    });

    if (!captionResponse.ok) {
      return res
        .status(captionResponse.status)
        .json({ error: `Caption API Error: ${captionResponse.statusText}` });
    }

    const captionData = await captionResponse.json();
    const transcript = JSON.stringify(captionData);

    // 3. Send Transcript to Gemini AI
    const geminiApiKey = process.env.GEMINI_API_KEY;
    const geminiUrl = `https://generativelanguage.googleapis.com/v1/models/gemini-3.1-flash-lite:generateContent?key=${geminiApiKey}`;

    const prompt = `You are a professional academic note-taker. Convert the following transcript into clean, structured study notes.\n\nSTRICT RULES:\n1. Use ALL CAPS for headings.\n2. Use dashes (-) for bullet points.\n3. No markdown, no asterisks, no bold/italics.\n4. No intro, no outro, no echoing instructions.\n5. Use double line breaks between sections.\n\nREQUIRED FORMAT:\nYour entire response must follow this exact structure:\n<thought>\n[Your internal reasoning]\n</thought>\n<final>\n[The clean notes here]\n</final>\n\nEXAMPLE:\n<thought>I will summarize the React video focusing on hooks.</thought>\n<final>\nREACT HOOKS\n- useState manages state.\n- useEffect handles side effects.\n</final>\n\nTranscript:\n${transcript}`;

    const geminiResponse = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [
          {
            parts: [{ text: prompt }],
          },
        ],
      }),
    });

    if (!geminiResponse.ok) {
      const errorText = await geminiResponse.text();
      return res
        .status(geminiResponse.status)
        .json({ error: `Gemini API Error: ${errorText}` });
    }

    const geminiData = await geminiResponse.json();
    const fullText =
      geminiData.candidates?.[0]?.content?.parts?.[0]?.text || "";

    // 4. Parse <final> tags
    const finalPattern = /<final\s*>(.*?)<\/final\s*>/is;
    const finalMatch = fullText.match(finalPattern);
    const cleanSummary = finalMatch
      ? finalMatch[1].trim()
      : fullText.replace(/\*/g, "").trim();

    return res.status(200).json({ summary: cleanSummary });
  } catch (error) {
    console.error("Server Error:", error);
    return res
      .status(500)
      .json({ error: "Internal Server Error: " + error.message });
  }
}
