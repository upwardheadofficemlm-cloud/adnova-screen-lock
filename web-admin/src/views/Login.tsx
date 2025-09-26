import { Box, Button, Card, CardContent, Container, TextField, Typography } from '@mui/material'
import { signInWithEmailAndPassword } from 'firebase/auth'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { auth } from '../firebase'

export function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await signInWithEmailAndPassword(auth, email, password)
      navigate('/')
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Container maxWidth="sm" sx={{ display: 'flex', alignItems: 'center', minHeight: '100vh' }}>
      <Card sx={{ width: '100%', p: 1 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>Sign in</Typography>
          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'grid', gap: 2 }}>
            <TextField label="Email" type="email" value={email} onChange={e => setEmail(e.target.value)} required fullWidth />
            <TextField label="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} required fullWidth />
            {error && <Typography color="error" variant="body2">{error}</Typography>}
            <Button type="submit" variant="contained" disabled={loading}>{loading ? 'Signing in...' : 'Sign in'}</Button>
          </Box>
        </CardContent>
      </Card>
    </Container>
  )
}

