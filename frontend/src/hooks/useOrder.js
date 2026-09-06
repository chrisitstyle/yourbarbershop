import { useQuery } from "@tanstack/react-query";

import { getOrderById } from "../api/orderService";

const isValidOrderId = (orderId) => Number.isInteger(orderId) && orderId > 0;

const useOrder = (orderId, initialData) => {
  const hasMatchingInitialData =
    isValidOrderId(orderId) && initialData?.idOrder === orderId;

  return useQuery({
    queryKey: ["orders", orderId],
    queryFn: () => getOrderById(orderId),
    enabled: isValidOrderId(orderId),
    initialData: hasMatchingInitialData ? initialData : undefined,
  });
};

export default useOrder;
