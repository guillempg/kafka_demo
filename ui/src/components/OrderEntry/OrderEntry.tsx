import { withFormik, FormikProps, Form as BaseForm, Field } from 'formik'
import { pick } from 'lodash'
import React from 'react'
import * as Yup from 'yup'
import './OrderEntry.css'

const BASE_URL = import.meta.env.VITE_BACKEND_URL

interface OrderEntryState {
  symbol: string
  quantity: number
  side: 'BUY' | 'SELL'
  owner: string
}

const Form = (props: FormikProps<OrderEntryState>) => {
  const { touched, errors, values } = props
  return (
    <BaseForm className='order-entry-form'>
      <label htmlFor='user'>User</label>
      <Field
        id='user'
        type='text'
        name='user'
        disabled={true}
        value={values.owner}
      />

      <label htmlFor='symbol'>Symbol</label>
      <Field id='symbol' type='text' name='symbol' />
      {touched.symbol && errors.symbol && <div>{errors.symbol}</div>}

      <label htmlFor='quantity'>Quantity</label>
      <Field id='quantity' type='text' name='quantity' />
      {touched.quantity && errors.quantity && <div>{errors.quantity}</div>}

      <label htmlFor='side'>Side</label>
      <Field id='side' name='side' as='select'>
        <option value='BUY'>BUY</option>
        <option value='SELL'>SELL</option>
      </Field>
      {touched.side && errors.side && <div>{errors.side}</div>}

      <button type='submit'>Submit</button>
    </BaseForm>
  )
}

interface OrderEntryProps extends OrderEntryState {
  setResponse: React.Dispatch<React.SetStateAction<string>>
}

const headers = new Headers({
  'Access-Control-Allow-Origin': BASE_URL,
  'Access-Control-Allow-Credentials': 'true',
  'Content-Type': 'application/json; charset=utf-8',
})

export const OrderEntry = withFormik<OrderEntryProps, OrderEntryState>({
  mapPropsToValues: (props: OrderEntryProps) => {
    return {
      ...pick(props, 'symbol', 'quantity', 'side', 'owner'),
    }
  },
  validationSchema: (props: OrderEntryProps) => {
    props.setResponse('')
    return Yup.object().shape({
      symbol: Yup.string().required('Required'),
      quantity: Yup.number()
        .min(1, 'Must be greater than 0')
        .integer('Must be an integer')
        .required('Required'),
      side: Yup.string().required('Required'),
    })
  },
  handleSubmit: (values, formikBag) => {
    fetch(`${BASE_URL}/orders/place`, {
      method: 'POST',
      body: JSON.stringify(values),
      mode: 'cors',
      headers: headers,
    })
      .then((res) => res.json())
      .then((response) =>
        formikBag.props.setResponse('Response: ' + JSON.stringify(response))
      )
      .catch((err) =>
        formikBag.props.setResponse('Error: ' + JSON.stringify(err))
      )
    formikBag.resetForm()
  },
  validateOnBlur: true,
  validateOnChange: true,
})(Form)
