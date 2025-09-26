import { onAuthStateChanged } from 'firebase/auth'
import { ReactNode, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { auth } from '../firebase'

export function RequireAuth({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const [ready, setReady] = useState(false)
  useEffect(() => {
    return onAuthStateChanged(auth, user => {
      if (!user) navigate('/login')
      else setReady(true)
    })
  }, [navigate])
  if (!ready) return null
  return <>{children}</>
}

