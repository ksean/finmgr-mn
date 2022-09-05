/*
    finmgr - a financial management framework
    Copyright (C) 2022  Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    sean <at> kennedy <dot> software
 */
import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import InvestmentTransactions from "../InvestmentTransactions";
import * as React from "react";
import PortfolioHistory from "../PortfolioHistory";

export default function Home() {
    return (
        <Grid container spacing={3}>
            {/* Portfolio History */}
            <Grid item xs={12} md={8} lg={12}>
                <Paper sx={{p: 2, display: 'flex', flexDirection: 'column', height: 480}}>
                    <PortfolioHistory/>
                </Paper>
            </Grid>
            {/* Recent Transaction */}
            <Grid item xs={'auto'}>
                <Paper sx={{p: 2, display: 'flex', flexDirection: 'column'}}>
                    <InvestmentTransactions/>
                </Paper>
            </Grid>
        </Grid>
    );
}