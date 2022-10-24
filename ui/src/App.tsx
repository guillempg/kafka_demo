import { CancelOrder, OrderEntry } from './components'
import './App.css'
import { useState } from 'react'

interface formState {
  symbol: string
  quantity: number
  side: 'BUY' | 'SELL'
  owner: string
}

interface cancelOrderState {
  orderId: string
}

function App() {
  const initialOrderEntryValues: formState = {
    symbol: '',
    quantity: 0,
    side: 'BUY',
    owner: 'TestUser',
  }

  const [orderEntryResponse, setOrderEntryResponse] = useState<string>('')
  const [cancelOrderResponse, setCancelOrderResponse] = useState<string>('')

  const initialCancelOrderValues: cancelOrderState = {
    orderId: '',
  }

  return (
    <div>
      <section>
        <h2>Order Entry</h2>
        <OrderEntry
          {...initialOrderEntryValues}
          setResponse={setOrderEntryResponse}
        />
        <p>{orderEntryResponse}</p>
      </section>
      <section>
        <h2>Cancel Order</h2>
        <CancelOrder
          {...initialCancelOrderValues}
          setResponse={setCancelOrderResponse}
        />
        <p>{cancelOrderResponse}</p>
      </section>
    </div>
  )
}

export default App
