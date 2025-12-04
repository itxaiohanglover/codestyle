<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
import HeroSection from '@/components/HeroSection.vue'
import SupportedIDEs from '@/components/SupportedIDEs.vue'
import ProblemSection from '@/components/ProblemSection.vue'
import SolutionSection from '@/components/SolutionSection.vue'
import UseCasesSection from '@/components/UseCasesSection.vue'
import TechLogicSection from '@/components/TechLogicSection.vue'
import HowItWorks from '@/components/HowItWorks.vue'
// import PricingSection from '@/components/PricingSection.vue'
// import CTASection from '@/components/CTASection.vue'
import FAQSection from '@/components/FAQSection.vue'
import Footer from '@/components/Footer.vue'
</script>

<template>
  <div class="min-h-screen">
    <Navbar />
    <main>
      <HeroSection />
      <SupportedIDEs />
      <ProblemSection />
      <SolutionSection />
      <UseCasesSection />
      <TechLogicSection />
      <HowItWorks />
      <!-- <PricingSection /> -->
      <!-- <CTASection /> -->
      <FAQSection />
    </main>
    <Footer/>
  </div>
</template>
