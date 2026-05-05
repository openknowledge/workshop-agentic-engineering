import { z } from 'zod';

export const customerSchema = z.object({
  firstName: z
    .string()
    .min(1, 'Vorname ist erforderlich')
    .max(255, 'Vorname darf maximal 255 Zeichen lang sein'),
  lastName: z
    .string()
    .min(1, 'Nachname ist erforderlich')
    .max(255, 'Nachname darf maximal 255 Zeichen lang sein'),
  email: z
    .string()
    .min(1, 'E-Mail ist erforderlich')
    .email('Ungültige E-Mail-Adresse')
    .max(255, 'E-Mail darf maximal 255 Zeichen lang sein'),
  phoneNumber: z
    .string()
    .max(50, 'Telefonnummer darf maximal 50 Zeichen lang sein')
    .or(z.undefined()),
  birthDate: z.string().or(z.undefined()),
});

export type Customer = z.infer<typeof customerSchema>;
