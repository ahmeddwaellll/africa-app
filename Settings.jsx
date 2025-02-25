import React from 'react';
import { Container, Typography } from '@mui/material';

function Settings() {
  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Settings
      </Typography>
      <Typography variant="body1">
        App settings will be displayed here.
      </Typography>
    </Container>
  );
}

export default Settings;
