export interface TradeContact {
  id: number;
  whatsappNumber: string;
  displayName: string | null;
  businessName: string | null;
  contactType: string;
  city: string | null;
  commodityInterest: string | null;
  lifetimeValue: number;
  totalOrders: number;
  isActive: boolean;
}

export interface Order {
  id: number;
  orderReference: string;
  commodity: string;
  grade: string | null;
  quantity: number | null;
  unit: string | null;
  quotedPrice: number | null;
  finalPrice: number | null;
  totalAmount: number | null;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentTerms: string | null;
  deliveryTerms: string | null;
  notes: string | null;
  whatsappThreadId: string | null;
  createdAt: string;
  updatedAt: string;
  tradeContact: TradeContact;
}

export type OrderStatus =
  | 'INQUIRY'
  | 'QUOTED'
  | 'NEGOTIATING'
  | 'CONFIRMED'
  | 'DISPATCHED'
  | 'DELIVERED'
  | 'CANCELLED';

export type PaymentStatus =
  | 'PENDING'
  | 'PARTIAL'
  | 'PAID'
  | 'OVERDUE';

export interface PendingApproval {
  whatsappMessageId: string;
  fromNumber: string;
  suggestedReply: string | null;
  routingDecision: string;
  commodity: string | null;
  grade: string | null;
  finalPricePerUnit: number | null;
  totalAmount: number | null;
  unit: string | null;
  processedAt: string;
}

export interface FollowUpJob {
  id: number;
  jobType: string;
  messageTemplate: string;
  scheduledAt: string;
  executedAt: string | null;
  status: string;
  attemptCount: number;
}

export interface DashboardStats {
  totalOrders: number;
  quotedOrders: number;
  confirmedOrders: number;
  overduePayments: number;
  pendingFollowUps: number;
}