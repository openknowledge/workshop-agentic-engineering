import { z } from 'zod';

export const customerSchema = z
  .object({
    name: z.object({
      firstName: z
        .string()
        .min(1, 'Vorname ist erforderlich')
        .max(255, 'Vorname darf maximal 255 Zeichen lang sein')
        .regex(/^\D*$/, 'Vorname darf keine Ziffern enthalten'),
      lastName: z
        .string()
        .min(1, 'Nachname ist erforderlich')
        .max(255, 'Nachname darf maximal 255 Zeichen lang sein')
        .regex(/^\D*$/, 'Nachname darf keine Ziffern enthalten'),
    }),
    email: z
      .string()
      .min(1, 'E-Mail ist erforderlich')
      .email('Ungültige E-Mail-Adresse')
      .max(255, 'E-Mail darf maximal 255 Zeichen lang sein'),
    phoneNumber: z
      .string()
      .max(50, 'Telefonnummer darf maximal 50 Zeichen lang sein')
      .regex(
        /^\+?[0-9 ()/-]*$/,
        'Telefonnummer darf nur Ziffern, Leerzeichen und die Zeichen +()/- enthalten'
      )
      .nullable()
      .or(z.undefined()),
    birthDate: z.string().nullable().or(z.undefined()),
  })
  // Lets unknown fields (additionalEmails, additionalPhoneNumbers, externalUserId, ...)
  // round-trip through the client without the form needing to know their names.
  .catchall(z.unknown());

export type Customer = z.infer<typeof customerSchema>;
