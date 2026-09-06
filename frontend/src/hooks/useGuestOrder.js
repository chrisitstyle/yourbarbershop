import { useQuery } from "@tanstack/react-query";

import { getGuestOrderById } from "../api/guestOrderService";

const isValidGuestOrderId = (guestOrderId) =>
  Number.isInteger(guestOrderId) && guestOrderId > 0;

const useGuestOrder = (guestOrderId, initialData) => {
  const hasMatchingInitialData =
    isValidGuestOrderId(guestOrderId) &&
    initialData?.idGuestOrder === guestOrderId;

  return useQuery({
    queryKey: ["guestOrders", guestOrderId],
    queryFn: () => getGuestOrderById(guestOrderId),
    enabled: isValidGuestOrderId(guestOrderId),
    initialData: hasMatchingInitialData ? initialData : undefined,
  });
};

export default useGuestOrder;
