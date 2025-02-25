import React from 'react';
import { Container, Typography } from '@mui/material';

function Login() {
  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Login
      </Typography>
      <Typography variant="body1">
        Login form will be displayed here.
      </Typography>
    </Container>
  );
}

export default Login;
