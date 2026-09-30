package com.dobebets.api.betslip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database operations for the tipster's bet slips.
 */
public interface BetSlipRepository extends JpaRepository<BetSlip, Long> {

    /**
     * Lists slips in the requested publication state, newest first.
     */
    List<BetSlip> findByStatusOrderByIdDesc(BetSlipStatus status);

    /**
     * Lists premium slips in the requested publication state.
     */
    List<BetSlip> findByPremiumTrueAndStatusOrderByIdDesc(
            BetSlipStatus status);
}