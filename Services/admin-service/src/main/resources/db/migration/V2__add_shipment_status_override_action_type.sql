-- FR_AdminOverrideShipmentStatus -- audit action for admin shipment status override

ALTER TYPE admin_action_type ADD VALUE IF NOT EXISTS 'SHIPMENT_STATUS_OVERRIDE';