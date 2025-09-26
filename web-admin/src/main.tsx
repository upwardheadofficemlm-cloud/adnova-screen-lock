import React from 'react'
import ReactDOM from 'react-dom/client'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import { RouterProvider } from 'react-router-dom'
import { router } from './router'

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#0B5FFF' },
    secondary: { main: '#6E56CF' }
  },
  shape: { borderRadius: 12 }
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <RouterProvider router={router} />
    </ThemeProvider>
  </React.StrictMode>
)

