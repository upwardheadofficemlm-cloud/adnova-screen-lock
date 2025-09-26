import { Box, Button, Card, CardActions, CardContent, Grid, TextField, Typography } from '@mui/material'
import { collection, doc, getDocs, orderBy, query, serverTimestamp, setDoc } from 'firebase/firestore'
import { useEffect, useState } from 'react'
import { db } from '../firebase'

type Device = {
  id: string
  lastSeen?: any
  model?: string
}

export function Devices() {
  const [devices, setDevices] = useState<Device[]>([])
  const [targetId, setTargetId] = useState('')

  useEffect(() => {
    ;(async () => {
      const q = query(collection(db, 'devices'), orderBy('id'))
      const snap = await getDocs(q)
      const list = snap.docs.map(d => ({ id: d.id, ...d.data() })) as Device[]
      setDevices(list)
    })()
  }, [])

  async function sendCommand(deviceId: string, action: string) {
    const cmdRef = doc(collection(db, 'devices', deviceId, 'commands'))
    await setDoc(cmdRef, { action, state: 'pending', createdAt: serverTimestamp() })
    alert(`Sent ${action} to ${deviceId}`)
  }

  return (
    <Box sx={{ display: 'grid', gap: 2 }}>
      <Box sx={{ display: 'flex', gap: 1 }}>
        <TextField placeholder="Device ID" value={targetId} onChange={e => setTargetId(e.target.value)} size="small" />
        <Button variant="outlined" onClick={() => targetId && sendCommand(targetId, 'touchLock')}>Touch Lock</Button>
        <Button variant="outlined" onClick={() => targetId && sendCommand(targetId, 'touchUnlock')}>Touch Unlock</Button>
        <Button variant="outlined" onClick={() => targetId && sendCommand(targetId, 'sleep')}>Sleep</Button>
        <Button variant="outlined" onClick={() => targetId && sendCommand(targetId, 'wake')}>Wake</Button>
        <Button color="error" variant="contained" onClick={() => targetId && sendCommand(targetId, 'reboot')}>Reboot</Button>
      </Box>
      <Grid container spacing={2}>
        {devices.map(d => (
          <Grid item xs={12} sm={6} md={4} key={d.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{d.id}</Typography>
                <Typography variant="body2" color="text.secondary">{d.model || 'Unknown model'}</Typography>
              </CardContent>
              <CardActions>
                <Button size="small" onClick={() => sendCommand(d.id, 'touchLock')}>Lock</Button>
                <Button size="small" onClick={() => sendCommand(d.id, 'touchUnlock')}>Unlock</Button>
                <Button size="small" onClick={() => sendCommand(d.id, 'sleep')}>Sleep</Button>
                <Button size="small" onClick={() => sendCommand(d.id, 'wake')}>Wake</Button>
                <Button color="error" size="small" onClick={() => sendCommand(d.id, 'reboot')}>Reboot</Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}

