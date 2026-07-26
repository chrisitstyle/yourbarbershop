import { useState, useEffect } from "react";

/**
 * A hook to automatically clear state after a specified time.
 * @param {any} initialValue - The initial value (e.g., [] for a list of errors, null for a single error).
 * @param {number} timeout - Time in milliseconds after which the state should revert to the initial value (default 6000ms).
 * @returns [state, setState] - An array containing the state and the function to update it.
 */
const useAutoDismiss = (initialValue, timeout = 6000) => {
  const [state, setState] = useState(initialValue);

  useEffect(() => {
    // check if the state has any "content" (is not empty)
    const hasContent = Array.isArray(state) ? state.length > 0 : Boolean(state);

    // if there is content, start the timer
    if (hasContent) {
      const timer = setTimeout(() => {
        setState(initialValue); // Reset to the initial value (e.g., empty array)
      }, timeout);

      // cleanup - if the state changes in the meantime (e.g., user clicks the button again)
      // or the component unmounts, clear the timer to avoid overwriting the new state.
      return () => clearTimeout(timer);
    }
  }, [state, timeout, initialValue]);

  return [state, setState];
};

export default useAutoDismiss;
