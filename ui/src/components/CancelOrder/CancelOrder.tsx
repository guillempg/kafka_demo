import { Form as BaseForm, Field, withFormik, FormikProps } from 'formik'
import * as Yup from 'yup'
import './CancelOrder.css'

const BASE_URL = import.meta.env.VITE_BACKEND_URL

const headers = new Headers({
  'Access-Control-Allow-Origin': BASE_URL,
  'Access-Control-Allow-Credentials': 'true',
  'Content-Type': 'application/json; charset=utf-8',
})
interface CancelOrderState {
  orderId: string
}

const Form = (props: FormikProps<CancelOrderState>) => {
  const { touched, errors } = props
  return (
    <BaseForm className='cancel-order-form'>
      <label htmlFor='orderId'>Order ID</label>
      <Field id='orderId' type='text' name='orderId' />
      {touched.orderId && errors.orderId && <div>{errors.orderId}</div>}
      <button type='submit'>Cancel</button>
    </BaseForm>
  )
}

interface OrderCancelProps extends CancelOrderState {
  setResponse: React.Dispatch<React.SetStateAction<string>>
}

export const CancelOrder = withFormik<OrderCancelProps, CancelOrderState>({
  mapPropsToValues: (props) => {
    return {
      orderId: props.orderId,
    }
  },
  validationSchema: (props: OrderCancelProps) => {
    props.setResponse('')
    return Yup.object().shape({
      orderId: Yup.number()
        .min(1, 'Must be greater than 0')
        .integer('Must be an integer id')
        .required('Required'),
    })
  },
  handleSubmit: (values, formikBag) => {
    fetch(`${BASE_URL}/orders/cancel/${values.orderId}`, {
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
