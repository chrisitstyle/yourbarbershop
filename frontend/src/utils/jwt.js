// returns true if the token exists and has not expired, otherwise returns false

export function isTokenValid(token) {
  if (!token) return false;
  try {
    // only care about the payload from the JWT
    const payloadBase64 = token.split(".")[1];
    // add padding if length is not a multiple of 4 (needed for atob in browsers)
    const padLength = 4 - (payloadBase64.length % 4);
    const payloadCorrected =
      payloadBase64 + "=".repeat(padLength === 4 ? 0 : padLength);
    const payloadJson = atob(payloadCorrected);
    const payload = JSON.parse(payloadJson);

    if (!payload.exp) return false;
    return payload.exp * 1000 > Date.now();
  } catch (err) {
    // if parsing the token fails, return false
    return false;
  }
}
