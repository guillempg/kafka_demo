import { rest } from 'msw'

const BASE_URL = import.meta.env.VITE_BACKEND_URL

interface OrderEntryBody {
  id: string | null
  owner: string
  symbol: string
  quantity: number
  side: 'BUY' | 'SELL'
  orderType?: 'MARKET' | 'LIMIT'
}

interface OrderEntryResponse {
  id: string
  owner: string
  symbol: string
  quantity: string
  side: 'BUY' | 'SELL'
}

interface OrderCancelBody {
  orderId: string
}

interface OrderCancelResponse {
  orderId: string
  orderStatus: 'CANCELLED' | 'FILLED' | 'FAILURE' | 'PENDING'
}

export const handlers = [
  rest.post<OrderEntryBody, OrderEntryResponse>(
    `${BASE_URL}/orders/place`,
    async (req, res, ctx) => {
      const { quantity, side, symbol, owner } = await req.json<OrderEntryBody>()

      return res(
        ctx.json({
          id: '67423675493823',
          owner,
          symbol,
          quantity,
          side,
        })
      )
    }
  ),
  rest.post<OrderCancelBody, OrderCancelResponse>(
    `${BASE_URL}/orders/cancel/:orderId`,
    async (req, res, ctx) => {
      const { orderId } = await req.json<OrderCancelBody>()
      return res(
        ctx.json({
          orderId: orderId,
          orderStatus: 'CANCELLED',
        })
      )
    }
  ),
]
