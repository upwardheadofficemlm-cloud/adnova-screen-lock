import { createBrowserRouter } from 'react-router-dom'
import { AppShell } from './shell/AppShell'
import { RequireAuth } from './shell/RequireAuth'
import { Login } from './views/Login'
import { Devices } from './views/Devices'

export const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <RequireAuth>
        <AppShell />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <Devices /> }
    ]
  },
  { path: '/login', element: <Login /> }
])

