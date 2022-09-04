import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import InvestmentTransactions from "../InvestmentTransactions";
import * as React from "react";

export default function Home() {
    return (
        <Grid container spacing={3}>
            {/* Recent Transaction */}
            <Grid item xs={12}>
                <Paper sx={{p: 2, display: 'flex', flexDirection: 'column'}}>
                    <InvestmentTransactions/>
                </Paper>
            </Grid>
        </Grid>
    );
}