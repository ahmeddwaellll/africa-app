import React from 'react';
import { Container, Typography } from '@mui/material';

function Profile() {
  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Profile
      </Typography>
      <Typography variant="body1">
        User profile information will be displayed here.
      </Typography>
    </Container>
  );
}

export default Profile;
