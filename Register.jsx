import React from 'react';
import { Container, Typography } from '@mui/material';

function Register() {
  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Register
      </Typography>
      <Typography variant="body1">
        Registration form will be displayed here.
      </Typography>
    </Container>
  );
}

export default Register;
